# Phase 2: JWT 토큰 만들고 검증하기 🔐

> **목표**: JWT 토큰을 생성하고 유효성을 검증하는 핵심 기능을 만듭니다.

## 4단계: JWT 토큰 생성/검증 클래스 만들기

**📌 왜 필요한가요?**

- 로그인 성공 시 사용자에게 토큰을 발급해야 합니다
- 이후 요청에서 받은 토큰이 진짜인지 검증해야 합니다

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/config/security/JwtTokenProviderTest.java` (테스트 - 먼저 작성)
- `src/main/java/com/oneday/core/config/security/JwtTokenProvider.java` (실제 구현)

---

## ✅ 4-1. 테스트 먼저 작성하기 (TDD 방식)

### 💡 TDD란?

1. **Red**: 실패하는 테스트를 먼저 작성
2. **Green**: 테스트를 통과하는 최소한의 코드 작성
3. **Refactor**: 코드를 깔끔하게 정리

### 테스트 코드 작성

```java
package com.oneday.core.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {
    
    private JwtTokenProvider jwtTokenProvider;
    
    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // 테스트용 설정 값 주입
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", 
            "test-secret-key-for-jwt-token-generation-minimum-256-bits");
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", 604800000L);
        jwtTokenProvider.init(); // 키 초기화
    }
    
    // 1. 토큰을 만들 수 있는가?
    @Test
    void 토큰_생성_성공() {
        // Given: 사용자 이메일이 있을 때
        String email = "test@example.com";
        
        // When: 토큰을 생성하면
        String token = jwtTokenProvider.generateAccessToken(email);
        
        // Then: 토큰이 정상적으로 생성된다
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }
    
    // 2. 만든 토큰이 유효한가?
    @Test
    void 유효한_토큰_검증_성공() {
        // Given: 토큰을 생성했을 때
        String token = jwtTokenProvider.generateAccessToken("test@example.com");
        
        // When: 토큰을 검증하면
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        // Then: 유효하다고 판단된다
        assertThat(isValid).isTrue();
    }
    
    // 3. 토큰에서 사용자 정보를 꺼낼 수 있는가?
    @Test
    void 토큰에서_사용자명_추출_성공() {
        // Given: 특정 이메일로 토큰을 만들었을 때
        String email = "test@example.com";
        String token = jwtTokenProvider.generateAccessToken(email);
        
        // When: 토큰에서 이메일을 추출하면
        String extractedEmail = jwtTokenProvider.getUsernameFromToken(token);
        
        // Then: 원래 이메일이 나온다
        assertThat(extractedEmail).isEqualTo(email);
    }
    
    // 4. 잘못된 형식의 토큰은 거부되는가?
    @Test
    void 잘못된_토큰_검증_실패() {
        // Given: 잘못된 형식의 토큰이 있을 때
        String invalidToken = "잘못된.토큰.형식";
        
        // When: 토큰을 검증하면
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        // Then: 유효하지 않다고 판단된다
        assertThat(isValid).isFalse();
    }
    
    // 5. Refresh Token도 생성할 수 있는가?
    @Test
    void Refresh_Token_생성_성공() {
        // Given: 사용자 이메일이 있을 때
        String email = "test@example.com";
        
        // When: Refresh Token을 생성하면
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);
        
        // Then: 토큰이 정상적으로 생성된다
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
    }
}
```

---

## ✅ 4-2. 실제 기능 구현하기

### JwtTokenProvider 구현

```java
package com.oneday.core.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    
    private Key key;
    
    // 애플리케이션 시작 시 비밀키 초기화
    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Access Token 생성
     * @param email 사용자 이메일
     * @return JWT Access Token
     */
    public String generateAccessToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
        
        return Jwts.builder()
                .setSubject(email)              // 토큰 주제 (사용자 이메일)
                .setIssuedAt(now)               // 발급 시간
                .setExpiration(expiryDate)      // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256)  // 서명
                .compact();
    }
    
    /**
     * Refresh Token 생성
     * @param email 사용자 이메일
     * @return JWT Refresh Token
     */
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);
        
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 토큰 유효성 검증
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.", e);
        }
        return false;
    }
    
    /**
     * 토큰에서 사용자 이메일 추출
     * @param token JWT 토큰
     * @return 사용자 이메일
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    /**
     * 토큰 만료 시간 가져오기
     * @param token JWT 토큰
     * @return 만료 시간
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getExpiration();
    }
}
```

**💡 용어 설명**:

- **HS256**: 토큰에 서명하는 암호화 알고리즘
- **Claims**: 토큰에 담긴 정보 (예: 이메일, 만료시간)
- **서명(Signature)**: 토큰이 위조되지 않았음을 증명하는 값
- **@PostConstruct**: 빈 생성 후 자동으로 실행되는 초기화 메서드

---

## 5단계: 사용자 정보 조회 서비스 만들기

**📌 왜 필요한가요?**

- 로그인할 때 이메일로 사용자를 찾아야 합니다
- Spring Security가 사용자 정보를 가져올 때 사용합니다

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/service/user/CustomUserDetailsServiceTest.java` (테스트)
- `src/main/java/com/oneday/core/service/user/CustomUserDetailsService.java` (구현)

---

## ✅ 5-1. 테스트 작성

```java
package com.oneday.core.service.user;

import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;
import com.oneday.core.repository.user.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CustomUserDetailsService customUserDetailsService;

  // 1. 이메일로 사용자를 찾을 수 있는가?
  @Test
  void 이메일로_사용자_조회_성공() {
    // Given: 데이터베이스에 사용자가 있을 때
    User user = createUser("test@example.com");
    given(userRepository.findByEmail("test@example.com"))
      .willReturn(Optional.of(user));

    // When: 이메일로 사용자를 찾으면
    UserDetails userDetails = customUserDetailsService
      .loadUserByUsername("test@example.com");

    // Then: 사용자 정보가 반환된다
    assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
    assertThat(userDetails.getAuthorities()).isNotEmpty();
  }

  // 2. 없는 사용자를 찾으면 에러가 나는가?
  @Test
  void 존재하지_않는_사용자_예외_발생() {
    // Given: 데이터베이스에 사용자가 없을 때
    given(userRepository.findByEmail(anyString()))
      .willReturn(Optional.empty());

    // When & Then: 조회 시 예외가 발생한다
    assertThatThrownBy(() ->
      customUserDetailsService.loadUserByUsername("none@example.com"))
      .isInstanceOf(UsernameNotFoundException.class)
      .hasMessageContaining("사용자를 찾을 수 없습니다");
  }

  private User createUser(String email) {
    // 테스트용 User 객체 생성 로직
    return User.builder()
      .email(email)
      .password("encodedPassword")
      .name("테스트")
      .role(Role.USER)
      .build();
  }
}
```

---

## ✅ 5-2. CustomUserDetailsService 구현

```java
package com.oneday.core.service.user;

import com.oneday.core.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * 이메일로 사용자 정보 조회
   * Spring Security가 인증할 때 자동으로 호출됩니다
   */
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository.findByEmail(email)
      .orElseThrow(() -> new UsernameNotFoundException(
        "사용자를 찾을 수 없습니다: " + email));
  }
}
```

**💡 용어 설명**:

- **UserDetailsService**: Spring Security가 사용자 정보를 조회하는 표준 인터페이스
- **@Transactional(readOnly = true)**: 읽기 전용 트랜잭션 (성능 최적화)

---

## ✅ Phase 2 체크리스트

- [ ] `JwtTokenProviderTest.java` 작성 (5개 테스트)
- [ ] `JwtTokenProvider.java` 구현
- [ ] 모든 테스트 실행 → ✅ 통과 확인
- [ ] `CustomUserDetailsServiceTest.java` 작성 (2개 테스트)
- [ ] `CustomUserDetailsService.java` 구현
- [ ] 모든 테스트 실행 → ✅ 통과 확인
- [ ] 코드 리뷰 및 리팩토링

---

## 💡 테스트 실행 방법

### Gradle로 테스트 실행

```bash
# Windows
gradlew test --tests "JwtTokenProviderTest"
gradlew test --tests "CustomUserDetailsServiceTest"

# 전체 테스트 실행
gradlew test
```

### IDE에서 실행

- 테스트 클래스에서 우클릭 → "Run Tests"
- 또는 각 테스트 메서드 왼쪽의 ▶️ 버튼 클릭

---

## 다음 단계

✅ Phase 2 완료 후 → **[Phase 3: 회원가입 기능](phase3_signup.md)** 로 이동하세요!

