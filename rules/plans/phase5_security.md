# Phase 5: JWT 필터와 보안 설정 🛡️

> **목표**: 모든 API 요청에서 JWT 토큰을 확인하고, 유효한 사용자만 접근할 수 있게 합니다.

## 12단계: JWT 검사 필터 만들기

**📌 왜 필요한가요?**

- 모든 API 요청이 들어올 때마다 자동으로 토큰을 확인해야 합니다
- 필터는 요청이 Controller에 도달하기 전에 먼저 실행됩니다

### 💡 필터의 역할

```
클라이언트 요청
    ↓
1. JWT 필터가 요청을 가로챔
    ↓
2. Authorization 헤더에서 토큰 추출
    ↓
3. 토큰이 유효한지 검증
    ↓
4-1. 유효하면 → Spring Security에 인증 정보 등록
4-2. 유효하지 않으면 → 그냥 통과 (Controller에서 401 에러)
    ↓
5. Controller로 전달
```

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/config/security/JwtAuthenticationFilterTest.java` (테스트)
- `src/main/java/com/oneday/core/config/security/JwtAuthenticationFilter.java` (구현)

---

## ✅ 12-1. 필터 테스트 작성

```java
package com.oneday.core.config.security;

import com.oneday.core.service.user.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @Mock
    private CustomUserDetailsService userDetailsService;
    
    @Mock
    private FilterChain filterChain;
    
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    
    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }
    
    // 1. 유효한 토큰이 있으면 인증이 되는가?
    @Test
    void 유효한_토큰으로_인증_성공() throws ServletException, IOException {
        // Given: 유효한 토큰이 있을 때
        String token = "valid-jwt-token";
        String email = "test@example.com";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        UserDetails userDetails = User.builder()
            .username(email)
            .password("password")
            .authorities(Collections.emptyList())
            .build();
        
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getUsernameFromToken(token)).willReturn(email);
        given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);
        
        // When: 필터를 통과하면
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then: SecurityContext에 인증 정보가 설정된다
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(email);
        assertThat(auth.isAuthenticated()).isTrue();
        
        // 다음 필터로 전달되었는지 확인
        verify(filterChain).doFilter(request, response);
    }
    
    // 2. 토큰이 없어도 필터는 통과하는가?
    @Test
    void 토큰_없을_때_필터_통과() throws ServletException, IOException {
        // Given: Authorization 헤더가 없을 때
        // (request에 헤더를 추가하지 않음)
        
        // When: 필터를 통과하면
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then: 인증 정보는 없지만 필터는 통과한다
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        // 다음 필터로 전달되었는지 확인
        verify(filterChain).doFilter(request, response);
        
        // JWT 검증은 호출되지 않음
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }
    
    // 3. 만료된 토큰은 거부되는가?
    @Test
    void 만료된_토큰_인증_실패() throws ServletException, IOException {
        // Given: 만료된 토큰이 있을 때
        String expiredToken = "expired-jwt-token";
        request.addHeader("Authorization", "Bearer " + expiredToken);
        
        given(jwtTokenProvider.validateToken(expiredToken)).willReturn(false);
        
        // When: 필터를 통과하면
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then: 인증 정보가 설정되지 않는다
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        // 다음 필터로는 전달됨
        verify(filterChain).doFilter(request, response);
    }
    
    // 4. Bearer가 없는 토큰은 무시되는가?
    @Test
    void Bearer_없는_토큰_무시() throws ServletException, IOException {
        // Given: Bearer 없이 토큰만 있을 때
        request.addHeader("Authorization", "just-token-without-bearer");
        
        // When: 필터를 통과하면
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then: 토큰 검증이 호출되지 않음
        verify(jwtTokenProvider, never()).validateToken(anyString());
        
        // 인증 정보도 없음
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
    }
}
```

---

## ✅ 12-2. JwtAuthenticationFilter 구현

```java
package com.oneday.core.config.security;

import com.oneday.core.service.user.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * 모든 HTTP 요청에 대해 JWT 토큰을 검증하고 인증 정보를 설정합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String token = extractTokenFromRequest(request);
            
            // 2. 토큰이 있고 유효한지 확인
            if (token != null && jwtTokenProvider.validateToken(token)) {
                
                // 3. 토큰에서 사용자 이메일 추출
                String email = jwtTokenProvider.getUsernameFromToken(token);
                
                // 4. 사용자 정보 조회
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                // 5. Spring Security에 인증 정보 등록
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT 인증 성공: email={}", email);
            }
        } catch (Exception e) {
            log.error("JWT 인증 실패", e);
        }
        
        // 6. 다음 필터로 전달
        filterChain.doFilter(request, response);
    }
    
    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 이후 부분 추출
        }
        
        return null;
    }
}
```

**💡 용어 설명**:

- **OncePerRequestFilter**: 요청당 한 번만 실행되는 필터
- **SecurityContext**: Spring Security가 현재 인증된 사용자 정보를 저장하는 곳
- **UsernamePasswordAuthenticationToken**: 인증 정보를 담는 객체

---

## 13단계: Spring Security 설정하기

**📌 왜 필요한가요?**

- Spring Security에게 우리가 만든 JWT 필터를 사용하라고 알려줘야 합니다
- 어떤 URL은 인증 없이 접근 가능하고, 어떤 URL은 인증이 필요한지 설정합니다

**📝 작업할 파일**:

- `src/main/java/com/oneday/core/config/security/SecurityConfig.java`

---

## ✅ 13-1. SecurityConfig 구현

```java
package com.oneday.core.config.security;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * Security 필터 체인 설정
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      // CSRF 보호 비활성화 (JWT 사용 시 불필요)
      .csrf(AbstractHttpConfigurer::disable)

      // 세션 사용 안 함 (JWT는 Stateless)
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

      // URL별 접근 권한 설정
      .authorizeHttpRequests(auth -> auth
        // 인증 없이 접근 가능한 URL
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
        .requestMatchers("/h2-console/**").permitAll()

        // 나머지는 인증 필요
        .anyRequest().authenticated())

      // H2 콘솔 사용을 위한 설정
      .headers(headers -> headers
        .frameOptions(frame -> frame.sameOrigin()))

      // JWT 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
      .addFilterBefore(jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * 비밀번호 암호화 도구
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * 인증 관리자
   */
  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}
```

**💡 용어 설명**:

- **CSRF**: 위조 요청 방지 기능 (JWT 사용 시 불필요)
- **Stateless**: 서버가 세션을 저장하지 않음 (JWT에 모든 정보 포함)
- **FilterChain**: 여러 필터가 순서대로 실행되는 체인
- **permitAll()**: 누구나 접근 가능
- **authenticated()**: 인증된 사용자만 접근 가능

---

## ✅ Phase 5 체크리스트

- [ ] `JwtAuthenticationFilterTest.java` 작성 (4개 테스트)
- [ ] `JwtAuthenticationFilter.java` 구현
- [ ] 모든 테스트 실행 → ✅ 통과 확인
- [ ] `SecurityConfig.java` 작성
- [ ] 애플리케이션 실행 → 에러 없는지 확인
- [ ] Postman으로 실제 동작 테스트

---

## 💡 Postman으로 테스트하기

### 1. 회원가입 (인증 불필요)

```
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

✅ 성공 (201 Created)

---

### 2. 로그인해서 토큰 받기

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

✅ 성공 (200 OK)
→ **accessToken을 복사하세요!**

---

### 3. 보호된 API 호출 (토큰 없이)

```
GET http://localhost:8080/api/users/me
```

❌ 실패 (401 Unauthorized)

---

### 4. 보호된 API 호출 (토큰 있음)

```
GET http://localhost:8080/api/users/me
Authorization: Bearer {여기에_토큰_붙여넣기}
```

✅ 성공 (200 OK)

---

## 💡 문제 해결

### "401 Unauthorized" 에러가 계속 나요

1. 토큰이 만료되지 않았나요? → 다시 로그인해서 새 토큰 받기
2. Authorization 헤더 형식이 맞나요? → `Bearer {토큰}` 형식 확인
3. 토큰 앞뒤에 공백이 있나요? → 공백 제거

### 필터가 실행되지 않아요

1. SecurityConfig에 필터를 추가했나요? → `addFilterBefore` 확인
2. 필터에 @Component가 붙어있나요? → 어노테이션 확인

---

## 다음 단계

✅ Phase 5 완료 후 → **[Phase 6: 에러 처리](phase6_error_handling.md)** 로 이동하세요!

