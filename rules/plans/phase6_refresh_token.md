# Phase 6: Refresh Token 갱신 API 🔄

> **목표**: Access Token이 만료되었을 때 Refresh Token으로 새로운 토큰을 발급받습니다.

## 왜 Refresh Token이 필요한가요?

**보안과 사용성의 균형**:

- **Access Token**: 짧은 유효기간 (1시간) → 탈취되어도 피해 최소화
- **Refresh Token**: 긴 유효기간 (7일) → 자주 로그인하지 않아도 됨

```
Access Token 만료
    ↓
Refresh Token으로 갱신 요청
    ↓
새로운 Access Token 발급
    ↓
계속 서비스 이용
```

---

## 13단계: Refresh Token 저장소 만들기

**📌 왜 필요한가요?**

- Refresh Token을 DB에 저장하여 관리합니다
- 로그아웃 시 Refresh Token을 무효화할 수 있습니다
- 탈취된 토큰을 차단할 수 있습니다

**📝 작업할 파일**:

- `src/main/java/com/oneday/core/entity/RefreshToken.java`
- `src/main/java/com/oneday/core/repository/RefreshTokenRepository.java`

---

## ✅ 13-1. RefreshToken Entity 만들기

```java
package com.oneday.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
  name = "refresh_tokens",
  indexes = {
    @Index(name = "idx_token", columnList = "token"),
    @Index(name = "idx_user_email", columnList = "userEmail")
  }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 500)
  private String token;

  @Column(nullable = false)
  private final String userEmail;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false, updatable = false)
  private final LocalDateTime createdAt;

  @Builder
  public RefreshToken(String token, String userEmail, LocalDateTime expiresAt) {
    this.token = token;
    this.userEmail = userEmail;
    this.expiresAt = expiresAt;
    this.createdAt = LocalDateTime.now();
  }

  // 토큰이 만료되었는지 확인
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(this.expiresAt);
  }

  // 토큰 갱신 (새로운 토큰과 만료 시간 설정)
  public void update(String newToken, LocalDateTime newExpiresAt) {
    this.token = newToken;
    this.expiresAt = newExpiresAt;
  }
}
```

**💡 설계 포인트**:

- **token 컬럼**: unique 제약으로 중복 방지
- **userEmail**: 사용자별 토큰 조회
- **expiresAt**: 만료 시간 저장
- **인덱스**: token, userEmail 조회 성능 최적화

---

## ✅ 13-2. RefreshTokenRepository 만들기

```java
package com.oneday.core.repository;

import com.oneday.core.entity.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  // 토큰으로 조회
  Optional<RefreshToken> findByToken(String token);

  // 사용자 이메일로 조회 (최신 토큰 1개)
  Optional<RefreshToken> findByUserEmail(String userEmail);

  // 사용자의 모든 토큰 삭제 (로그아웃)
  void deleteByUserEmail(String userEmail);

  // 만료된 토큰 일괄 삭제 (스케줄러에서 사용)
  void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
```

---

## 14단계: Token Refresh DTO 만들기

**📌 왜 필요한가요?**

- Refresh Token 요청과 응답의 형식을 정의합니다
- API 명세를 명확히 합니다

**📝 작업할 파일**:

- `src/main/java/com/oneday/core/dto/auth/TokenRefreshRequest.java`
- `src/main/java/com/oneday/core/dto/auth/TokenRefreshResponse.java`

---

## ✅ 14-1. TokenRefreshRequest 만들기

```java
package com.oneday.core.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh Token 갱신 요청 DTO
 */
public record TokenRefreshRequest(
  @NotBlank(message = "Refresh Token은 필수입니다")
  String refreshToken
) {
}
```

---

## ✅ 14-2. TokenRefreshResponse 만들기

```java
package com.oneday.core.dto.auth;

/**
 * Refresh Token 갱신 응답 DTO
 */
public record TokenRefreshResponse(
  String accessToken,
  String refreshToken,
  String tokenType,
  Long expiresIn
) {
  public TokenRefreshResponse(String accessToken, String refreshToken, Long expiresIn) {
    this(accessToken, refreshToken, "Bearer", expiresIn);
  }
}
```

---

## 15단계: Token Refresh 비즈니스 로직 (Service)

**📌 왜 필요한가요?**

- Refresh Token을 검증하고 새로운 Access Token을 발급합니다
- 보안을 위해 Refresh Token도 함께 갱신합니다 (Refresh Token Rotation)

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/service/auth/AuthServiceTest.java` (테스트 추가)
- `src/main/java/com/oneday/core/service/auth/AuthService.java` (구현 추가)
- `src/main/java/com/oneday/core/exception/auth/InvalidRefreshTokenException.java`

---

## ✅ 15-1. 예외 클래스 만들기

```java
package com.oneday.core.exception.auth;

public class InvalidRefreshTokenException extends RuntimeException {
  public InvalidRefreshTokenException(String message) {
    super(message);
  }
}
```

**GlobalExceptionHandler에 추가**:

```java

@ExceptionHandler(InvalidRefreshTokenException.class)
public ResponseEntity<ApiResponse<Void>> handleInvalidRefreshToken(
  InvalidRefreshTokenException ex) {

  ErrorResponse error = ErrorResponse.of(
    "AUTH006",
    ex.getMessage()
  );

  return ResponseEntity
    .status(HttpStatus.UNAUTHORIZED)
    .body(ApiResponse.error(error));
}
```

---

## ✅ 15-2. 테스트 작성

```java
// AuthServiceTest.java에 추가

@Mock
private RefreshTokenRepository refreshTokenRepository;

// 1. 유효한 Refresh Token으로 갱신 성공
@Test
void refresh_token_갱신_성공() {
  // Given: 유효한 Refresh Token이 있을 때
  String oldRefreshToken = "valid-refresh-token";
  String email = "test@example.com";

  RefreshToken refreshToken = RefreshToken.builder()
    .token(oldRefreshToken)
    .userEmail(email)
    .expiresAt(LocalDateTime.now().plusDays(7))
    .build();

  given(refreshTokenRepository.findByToken(oldRefreshToken))
    .willReturn(Optional.of(refreshToken));
  given(jwtTokenProvider.validateToken(oldRefreshToken)).willReturn(true);
  given(jwtTokenProvider.getUserEmailFromToken(oldRefreshToken)).willReturn(email);
  given(jwtTokenProvider.generateAccessToken(email)).willReturn("new-access-token");
  given(jwtTokenProvider.generateRefreshToken(email)).willReturn("new-refresh-token");
  given(jwtTokenProvider.getAccessTokenExpirationTime()).willReturn(3600L);

  TokenRefreshRequest request = new TokenRefreshRequest(oldRefreshToken);

  // When: 토큰 갱신 요청
  TokenRefreshResponse response = authService.refreshToken(request);

  // Then: 새로운 토큰이 발급된다
  assertThat(response.accessToken()).isEqualTo("new-access-token");
  assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
  assertThat(response.tokenType()).isEqualTo("Bearer");
  assertThat(response.expiresIn()).isEqualTo(3600L);

  // And: DB의 Refresh Token도 업데이트된다
  verify(refreshTokenRepository).save(any(RefreshToken.class));
}

// 2. 만료된 Refresh Token으로 요청 시 실패
@Test
void 만료된_refresh_token_예외_발생() {
  // Given: 만료된 Refresh Token
  String expiredToken = "expired-refresh-token";

  RefreshToken refreshToken = RefreshToken.builder()
    .token(expiredToken)
    .userEmail("test@example.com")
    .expiresAt(LocalDateTime.now().minusDays(1)) // 이미 만료됨
    .build();

  given(refreshTokenRepository.findByToken(expiredToken))
    .willReturn(Optional.of(refreshToken));

  TokenRefreshRequest request = new TokenRefreshRequest(expiredToken);

  // When & Then: 예외 발생
  assertThatThrownBy(() -> authService.refreshToken(request))
    .isInstanceOf(InvalidRefreshTokenException.class)
    .hasMessageContaining("만료된 Refresh Token입니다");
}

// 3. DB에 없는 Refresh Token으로 요청 시 실패
@Test
void 존재하지_않는_refresh_token_예외_발생() {
  // Given: DB에 없는 토큰
  String unknownToken = "unknown-token";

  given(refreshTokenRepository.findByToken(unknownToken))
    .willReturn(Optional.empty());

  TokenRefreshRequest request = new TokenRefreshRequest(unknownToken);

  // When & Then: 예외 발생
  assertThatThrownBy(() -> authService.refreshToken(request))
    .isInstanceOf(InvalidRefreshTokenException.class)
    .hasMessageContaining("유효하지 않은 Refresh Token입니다");
}

// 4. JWT 검증 실패 시 예외
@Test
void JWT_검증_실패_예외_발생() {
  // Given: JWT 형식은 맞지만 서명이 틀린 토큰
  String invalidToken = "invalid-jwt-token";

  RefreshToken refreshToken = RefreshToken.builder()
    .token(invalidToken)
    .userEmail("test@example.com")
    .expiresAt(LocalDateTime.now().plusDays(7))
    .build();

  given(refreshTokenRepository.findByToken(invalidToken))
    .willReturn(Optional.of(refreshToken));
  given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

  TokenRefreshRequest request = new TokenRefreshRequest(invalidToken);

  // When & Then: 예외 발생
  assertThatThrownBy(() -> authService.refreshToken(request))
    .isInstanceOf(InvalidRefreshTokenException.class)
    .hasMessageContaining("유효하지 않은 Refresh Token입니다");
}
```

---

## ✅ 15-3. Service 구현

```java
// AuthService.java에 추가

private final RefreshTokenRepository refreshTokenRepository;

/**
 * Refresh Token으로 새로운 Access Token 발급
 *
 * @param request Refresh Token 요청
 * @return 새로운 Access Token과 Refresh Token
 */
@Transactional
public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
  String refreshToken = request.refreshToken();

  // 1. DB에서 Refresh Token 조회
  RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
    .orElseThrow(() -> new InvalidRefreshTokenException(
      "유효하지 않은 Refresh Token입니다"));

  // 2. 만료 여부 확인
  if (savedToken.isExpired()) {
    refreshTokenRepository.delete(savedToken);
    throw new InvalidRefreshTokenException("만료된 Refresh Token입니다");
  }

  // 3. JWT 검증
  if (!jwtTokenProvider.validateToken(refreshToken)) {
    refreshTokenRepository.delete(savedToken);
    throw new InvalidRefreshTokenException("유효하지 않은 Refresh Token입니다");
  }

  // 4. 사용자 이메일 추출
  String email = jwtTokenProvider.getUserEmailFromToken(refreshToken);

  // 5. 새로운 토큰 발급
  String newAccessToken = jwtTokenProvider.generateAccessToken(email);
  String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

  // 6. Refresh Token Rotation: DB의 Refresh Token 업데이트
  LocalDateTime newExpiresAt = LocalDateTime.now()
    .plusSeconds(jwtTokenProvider.getRefreshTokenExpirationTime());

  savedToken.update(newRefreshToken, newExpiresAt);
  refreshTokenRepository.save(savedToken);

  log.info("토큰 갱신 완료: email={}", email);

  return new TokenRefreshResponse(
    newAccessToken,
    newRefreshToken,
    jwtTokenProvider.getAccessTokenExpirationTime()
  );
}
```

**💡 설계 포인트**:

- **Refresh Token Rotation**: 보안을 위해 Refresh Token도 함께 갱신
- **만료 토큰 삭제**: 만료되거나 유효하지 않은 토큰은 즉시 DB에서 삭제
- **트랜잭션**: DB 업데이트는 트랜잭션 내에서 안전하게 처리

---

## ✅ 15-4. JwtTokenProvider에 메서드 추가

```java
// JwtTokenProvider.java에 추가

/**
 * JWT 토큰에서 사용자 이메일 추출
 */
public String getUserEmailFromToken(String token) {
  Claims claims = parseClaims(token);
  return claims.getSubject();
}

/**
 * Refresh Token 유효 시간 (초 단위)
 */
public Long getRefreshTokenExpirationTime() {
  return refreshTokenExpiration / 1000; // milliseconds → seconds
}

/**
 * Access Token 유효 시간 (초 단위)
 */
public Long getAccessTokenExpirationTime() {
  return accessTokenExpiration / 1000; // milliseconds → seconds
}
```

---

## 16단계: Refresh Token API 만들기 (Controller)

**📌 왜 필요한가요?**

- 클라이언트가 토큰을 갱신할 수 있는 API를 제공합니다
- RESTful API 설계 원칙을 따릅니다

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/controller/auth/AuthControllerTest.java` (테스트 추가)
- `src/main/java/com/oneday/core/controller/auth/AuthController.java` (구현 추가)

---

## ✅ 16-1. Controller 테스트 작성

```java
// AuthControllerTest.java에 추가

// 1. 유효한 Refresh Token으로 갱신 성공
@Test
@DisplayName("토큰 갱신 API 성공")
void 토큰_갱신_API_성공() throws Exception {
  // Given: 유효한 Refresh Token으로 요청
  TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh-token");

  TokenRefreshResponse response = new TokenRefreshResponse(
    "new-access-token",
    "new-refresh-token",
    3600L
  );

  given(authService.refreshToken(any(TokenRefreshRequest.class)))
    .willReturn(response);

  // When & Then: POST /api/auth/refresh 호출
  mockMvc.perform(post("/api/auth/refresh")
      .with(csrf())
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(request)))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.success").value(true))
    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
    .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"))
    .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
}

// 2. 만료된 Refresh Token으로 요청 시 401
@Test
@DisplayName("토큰 갱신 실패 - 만료된 토큰")
void 토큰_갱신_실패_만료된_토큰() throws Exception {
  // Given: 만료된 Refresh Token
  TokenRefreshRequest request = new TokenRefreshRequest("expired-token");

  given(authService.refreshToken(any(TokenRefreshRequest.class)))
    .willThrow(new InvalidRefreshTokenException("만료된 Refresh Token입니다"));

  // When & Then: 401 Unauthorized
  mockMvc.perform(post("/api/auth/refresh")
      .with(csrf())
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(request)))
    .andExpect(status().isUnauthorized())
    .andExpect(jsonPath("$.success").value(false))
    .andExpect(jsonPath("$.error.code").value("AUTH006"));
}

// 3. 유효성 검증 실패 (빈 토큰)
@Test
@DisplayName("토큰 갱신 실패 - 빈 토큰")
void 토큰_갱신_실패_빈_토큰() throws Exception {
  // Given: Refresh Token이 없음
  TokenRefreshRequest request = new TokenRefreshRequest("");

  // When & Then: 400 Bad Request
  mockMvc.perform(post("/api/auth/refresh")
      .with(csrf())
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(request)))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.success").value(false));
}
```

---

## ✅ 16-2. Controller 구현

```java
// AuthController.java에 추가

/**
 * Refresh Token으로 Access Token 갱신
 *
 * @param request Refresh Token 요청
 * @return 새로운 Access Token과 Refresh Token
 */
@PostMapping("/refresh")
public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
  @Valid @RequestBody TokenRefreshRequest request) {

  log.info("토큰 갱신 API 호출");

  TokenRefreshResponse response = authService.refreshToken(request);

  return ResponseEntity.ok(ApiResponse.success(response));
}
```

---

## ✅ 16-3. SecurityConfig에 경로 추가

```java
// SecurityConfig.java 수정

.authorizeHttpRequests(auth ->auth
  .

requestMatchers("/swagger-ui/**").

permitAll()
    .

requestMatchers("/api/auth/signup","/api/auth/login","/api/auth/refresh").

permitAll()
    .

anyRequest().

authenticated()
)
```

---

## 17단계: 로그인 시 Refresh Token 저장 로직 추가

**📌 왜 필요한가요?**

- 로그인 시 발급한 Refresh Token을 DB에 저장해야 나중에 갱신할 수 있습니다

**📝 작업할 파일**:

- `src/main/java/com/oneday/core/service/auth/AuthService.java` (login 메서드 수정)

---

## ✅ 17-1. login 메서드 수정

```java
// AuthService.java의 login 메서드 수정

@Transactional
public LoginResponse login(LoginRequest request) {
  try {
    // 1. 인증 처리
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
        request.email(),
        request.password()
      )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    String email = authentication.getName();

    // 2. JWT 토큰 발급
    String accessToken = jwtTokenProvider.generateAccessToken(email);
    String refreshToken = jwtTokenProvider.generateRefreshToken(email);

    // 3. Refresh Token DB에 저장 (또는 업데이트)
    saveOrUpdateRefreshToken(email, refreshToken);

    log.info("로그인 성공: email={}", email);

    return LoginResponse.builder()
      .accessToken(accessToken)
      .refreshToken(refreshToken)
      .tokenType("Bearer")
      .expiresIn(jwtTokenProvider.getAccessTokenExpirationTime())
      .build();

  } catch (BadCredentialsException e) {
    log.warn("로그인 실패: email={}", request.email());
    throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다");
  }
}

/**
 * Refresh Token 저장 또는 업데이트
 */
private void saveOrUpdateRefreshToken(String email, String token) {
  LocalDateTime expiresAt = LocalDateTime.now()
    .plusSeconds(jwtTokenProvider.getRefreshTokenExpirationTime());

  // 기존 토큰이 있으면 업데이트, 없으면 새로 생성
  refreshTokenRepository.findByUserEmail(email)
    .ifPresentOrElse(
      existingToken -> {
        existingToken.update(token, expiresAt);
        refreshTokenRepository.save(existingToken);
      },
      () -> {
        RefreshToken newToken = RefreshToken.builder()
          .token(token)
          .userEmail(email)
          .expiresAt(expiresAt)
          .build();
        refreshTokenRepository.save(newToken);
      }
    );
}
```

---

## ✅ 완료 체크리스트

- [ ] RefreshToken Entity 생성
- [ ] RefreshTokenRepository 생성
- [ ] TokenRefreshRequest/Response DTO 생성
- [ ] InvalidRefreshTokenException 예외 생성
- [ ] GlobalExceptionHandler에 예외 핸들러 추가
- [ ] AuthService.refreshToken() 메서드 구현
- [ ] AuthService.login() 메서드에 Refresh Token 저장 로직 추가
- [ ] JwtTokenProvider에 헬퍼 메서드 추가
- [ ] AuthController.refreshToken() API 구현
- [ ] SecurityConfig에 /api/auth/refresh 경로 추가
- [ ] 단위 테스트 작성 및 통과
- [ ] Postman으로 수동 테스트

---

## 📝 API 명세

### POST /api/auth/refresh

**Request**:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200 OK)**:

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "error": null
}
```

**Error Response (401 Unauthorized)**:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH006",
    "message": "만료된 Refresh Token입니다"
  }
}
```

---

## 🧪 테스트 시나리오

### 정상 흐름

1. 로그인 → Access Token + Refresh Token 발급
2. Access Token으로 API 요청
3. Access Token 만료
4. Refresh Token으로 갱신 요청 → 새로운 토큰 발급
5. 새로운 Access Token으로 API 요청 계속

### 보안 시나리오

1. 만료된 Refresh Token → 401 에러
2. 탈취된 Refresh Token으로 갱신 시도 → Rotation으로 기존 토큰 무효화
3. 로그아웃한 사용자의 Refresh Token → 401 에러

---

## 💡 추가 고려사항

### Refresh Token Rotation

- 보안 강화를 위해 Refresh Token을 갱신할 때마다 새로운 토큰 발급
- 기존 토큰은 DB에서 업데이트되어 재사용 불가

### 만료 토큰 정리

- Phase 7 로그아웃에서 사용자별 토큰 삭제
- 스케줄러로 만료된 토큰 일괄 삭제 (선택사항)

### 동시성 제어

- 같은 사용자가 여러 기기에서 로그인 가능
- 기기별로 다른 Refresh Token 관리 (확장 가능)

---

## 🔗 다음 단계

Phase 7에서는 **로그아웃 API**를 구현합니다:

- Refresh Token 무효화
- SecurityContext 정리
- 블랙리스트 관리 (선택사항)

