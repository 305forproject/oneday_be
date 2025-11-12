# Phase 7: 로그아웃 API 🚪

> **목표**: 사용자가 로그아웃하면 Refresh Token을 무효화하여 더 이상 사용할 수 없게 합니다.

## 왜 로그아웃 API가 필요한가요?

**JWT의 특성과 보안**:

- JWT는 **무상태(Stateless)**이므로 서버에서 강제로 만료시킬 수 없습니다
- 하지만 **Refresh Token은 DB에 저장**되므로 삭제하여 무효화할 수 있습니다
- Access Token은 짧은 유효기간(1시간)이므로 자동으로 만료됩니다

```
로그아웃 요청
    ↓
Refresh Token DB에서 삭제
    ↓
Access Token은 만료될 때까지 유효 (최대 1시간)
    ↓
새로운 토큰 발급 불가 → 재로그인 필요
```

---

## 18단계: 로그아웃 DTO 만들기

**📌 왜 필요한가요?**

- 로그아웃 응답의 형식을 정의합니다
- API 일관성을 유지합니다

**📝 작업할 파일**:

- `src/main/java/com/oneday/core/dto/auth/LogoutResponse.java`

---

## ✅ 18-1. LogoutResponse 만들기

```java
package com.oneday.core.dto.auth;

import java.time.LocalDateTime;

/**
 * 로그아웃 응답 DTO
 */
public record LogoutResponse(
  String message,
  LocalDateTime logoutAt
) {
  public LogoutResponse(String message) {
    this(message, LocalDateTime.now());
  }

  public static LogoutResponse success() {
    return new LogoutResponse("로그아웃되었습니다");
  }
}
```

---

## 19단계: 로그아웃 비즈니스 로직 (Service)

**📌 왜 필요한가요?**

- Refresh Token을 DB에서 삭제합니다
- SecurityContext를 정리합니다
- 로그 기록을 남깁니다

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/service/auth/AuthServiceTest.java` (테스트 추가)
- `src/main/java/com/oneday/core/service/auth/AuthService.java` (구현 추가)

---

## ✅ 19-1. 테스트 작성

```java
// AuthServiceTest.java에 추가

// 1. 로그인한 사용자가 로그아웃 성공
@Test
void 로그아웃_성공() {
  // Given: 로그인한 사용자
  String email = "test@example.com";

  RefreshToken refreshToken = RefreshToken.builder()
    .token("refresh-token")
    .userEmail(email)
    .expiresAt(LocalDateTime.now().plusDays(7))
    .build();

  given(refreshTokenRepository.findByUserEmail(email))
    .willReturn(Optional.of(refreshToken));

  // When: 로그아웃
  LogoutResponse response = authService.logout(email);

  // Then: Refresh Token이 삭제된다
  verify(refreshTokenRepository).deleteByUserEmail(email);
  assertThat(response.message()).contains("로그아웃");
  assertThat(response.logoutAt()).isNotNull();
}

// 2. Refresh Token이 없어도 로그아웃 성공
@Test
void Refresh_Token_없어도_로그아웃_성공() {
  // Given: Refresh Token이 DB에 없는 사용자
  String email = "test@example.com";

  given(refreshTokenRepository.findByUserEmail(email))
    .willReturn(Optional.empty());

  // When: 로그아웃
  LogoutResponse response = authService.logout(email);

  // Then: 에러 없이 성공
  verify(refreshTokenRepository).deleteByUserEmail(email);
  assertThat(response.message()).contains("로그아웃");
}

// 3. 로그아웃 후 Refresh Token으로 갱신 시도하면 실패
@Test
void 로그아웃_후_토큰_갱신_실패() {
  // Given: 로그아웃된 사용자의 Refresh Token
  String token = "logged-out-token";

  given(refreshTokenRepository.findByToken(token))
    .willReturn(Optional.empty()); // 로그아웃으로 삭제됨

  TokenRefreshRequest request = new TokenRefreshRequest(token);

  // When & Then: 토큰 갱신 실패
  assertThatThrownBy(() -> authService.refreshToken(request))
    .isInstanceOf(InvalidRefreshTokenException.class)
    .hasMessageContaining("유효하지 않은 Refresh Token입니다");
}
```

---

## ✅ 19-2. Service 구현

```java
// AuthService.java에 추가

/**
 * 로그아웃 처리
 *
 * @param email 로그아웃할 사용자 이메일
 * @return 로그아웃 응답
 */
@Transactional
public LogoutResponse logout(String email) {
  log.info("로그아웃 시도: email={}", email);

  // 1. Refresh Token 삭제
  refreshTokenRepository.deleteByUserEmail(email);

  // 2. SecurityContext 정리
  SecurityContextHolder.clearContext();

  log.info("로그아웃 완료: email={}", email);

  return LogoutResponse.success();
}
```

**💡 설계 포인트**:

- **Refresh Token 삭제**: DB에서 사용자의 모든 Refresh Token 제거
- **SecurityContext 정리**: 현재 스레드의 인증 정보 제거
- **멱등성**: Refresh Token이 없어도 에러 없이 성공 (이미 로그아웃된 상태)

---

## 20단계: 로그아웃 API 만들기 (Controller)

**📌 왜 필요한가요?**

- 인증된 사용자만 로그아웃할 수 있습니다
- `@AuthenticationPrincipal`로 현재 로그인한 사용자 정보를 가져옵니다

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/controller/auth/AuthControllerTest.java` (테스트 추가)
- `src/main/java/com/oneday/core/controller/auth/AuthController.java` (구현 추가)

---

## ✅ 20-1. Controller 테스트 작성

```java
// AuthControllerTest.java에 추가

// 1. 인증된 사용자 로그아웃 성공
@Test
@DisplayName("로그아웃 API 성공")
void 로그아웃_API_성공() throws Exception {
  // Given: 로그인한 사용자
  String email = "test@example.com";

  LogoutResponse response = LogoutResponse.success();

  given(authService.logout(email)).willReturn(response);

  // When & Then: POST /api/auth/logout 호출
  mockMvc.perform(post("/api/auth/logout")
      .with(user(email).roles("USER"))
      .with(csrf())
      .contentType(MediaType.APPLICATION_JSON))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.success").value(true))
    .andExpect(jsonPath("$.data.message").value("로그아웃되었습니다"))
    .andExpect(jsonPath("$.data.logoutAt").exists());
}

// 2. 인증 없이 로그아웃 시도하면 실패
@Test
@DisplayName("로그아웃 실패 - 인증 없음")
void 로그아웃_실패_인증_없음() throws Exception {
  // Given: 인증 정보 없음

  // When & Then: 401 Unauthorized
  mockMvc.perform(post("/api/auth/logout")
      .with(csrf())
      .contentType(MediaType.APPLICATION_JSON))
    .andExpect(status().isForbidden()); // addFilters=false 환경에서는 403
}
```

---

## ✅ 20-2. Controller 구현

```java
// AuthController.java에 추가

/**
 * 로그아웃 API
 * 인증된 사용자의 Refresh Token을 무효화합니다
 *
 * @param userDetails Spring Security가 자동 주입
 * @return 로그아웃 응답
 */
@PostMapping("/logout")
public ResponseEntity<ApiResponse<LogoutResponse>> logout(
  @AuthenticationPrincipal UserDetails userDetails) {

  String email = userDetails.getUsername();
  log.info("로그아웃 API 호출: email={}", email);

  LogoutResponse response = authService.logout(email);

  return ResponseEntity.ok(ApiResponse.success(response));
}
```

---

## ✅ 20-3. SecurityConfig 확인

```java
// SecurityConfig.java 확인

.authorizeHttpRequests(auth ->auth
  .

requestMatchers("/swagger-ui/**").

permitAll()
    .

requestMatchers("/api/auth/signup","/api/auth/login","/api/auth/refresh").

permitAll()
    .

requestMatchers("/api/auth/logout","/api/auth/me").

authenticated()
    .

anyRequest().

authenticated()
)
```

**💡 설계 포인트**:

- `/api/auth/logout`은 **인증 필요** (authenticated)
- 로그인하지 않은 사용자는 로그아웃 불가 (401 에러)

---

## 21단계: 추가 보안 고려사항 (선택사항)

### ✅ 21-1. Access Token 블랙리스트 (고급)

**문제**: Access Token은 만료 전까지 유효하므로 로그아웃 후에도 최대 1시간 사용 가능

**해결**: Access Token 블랙리스트를 Redis에 저장 (선택사항)

```java
// AccessTokenBlacklist Entity (Redis)
@RedisHash(value = "blacklist", timeToLive = 3600) // 1시간
public class AccessTokenBlacklist {

  @Id
  private String token;

  private String userEmail;

  private LocalDateTime blacklistedAt;
}
```

```java
// JwtAuthenticationFilter에서 블랙리스트 확인
if(accessTokenBlacklistRepository.existsById(token)){
  log.

warn("블랙리스트된 토큰 사용 시도: {}",email);
    filterChain.

doFilter(request, response);
    return;
      }
```

**📌 트레이드오프**:

- 장점: 즉시 로그아웃 효과 (Access Token도 무효화)
- 단점: Redis 의존성 추가, 복잡도 증가, JWT의 Stateless 특성 상실

**권장**: 대부분의 경우 Refresh Token 삭제만으로 충분합니다.

---

### ✅ 21-2. 로그아웃 이벤트 로깅

```java
// LogoutEvent (선택사항)
@Getter
public class LogoutEvent extends ApplicationEvent {
  private final String email;
  private final LocalDateTime logoutAt;

  public LogoutEvent(Object source, String email) {
    super(source);
    this.email = email;
    this.logoutAt = LocalDateTime.now();
  }
}

// AuthService에서 이벤트 발행
@Transactional
public LogoutResponse logout(String email) {
  refreshTokenRepository.deleteByUserEmail(email);
  SecurityContextHolder.clearContext();

  // 이벤트 발행 (감사 로그, 분석 등에 활용)
  applicationEventPublisher.publishEvent(new LogoutEvent(this, email));

  return LogoutResponse.success();
}
```

---

### ✅ 21-3. 만료된 Refresh Token 자동 정리

```java
// RefreshTokenCleanupScheduler (선택사항)
@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupScheduler {

  private final RefreshTokenRepository refreshTokenRepository;

  /**
   * 매일 새벽 3시에 만료된 Refresh Token 삭제
   */
  @Scheduled(cron = "0 0 3 * * *")
  @Transactional
  public void cleanupExpiredTokens() {
    log.info("만료된 Refresh Token 정리 시작");

    LocalDateTime now = LocalDateTime.now();
    refreshTokenRepository.deleteByExpiresAtBefore(now);

    log.info("만료된 Refresh Token 정리 완료");
  }
}
```

**application.yml 설정 추가**:

```yaml
spring:
  task:
    scheduling:
      enabled: true
```

---

## ✅ 완료 체크리스트

- [ ] LogoutResponse DTO 생성
- [ ] AuthService.logout() 메서드 구현
- [ ] AuthController.logout() API 구현
- [ ] SecurityConfig에 /api/auth/logout 경로 권한 설정
- [ ] 단위 테스트 작성 및 통과
- [ ] Postman으로 수동 테스트
- [ ] (선택) Access Token 블랙리스트 구현
- [ ] (선택) 로그아웃 이벤트 로깅
- [ ] (선택) 만료 토큰 자동 정리 스케줄러

---

## 📝 API 명세

### POST /api/auth/logout

**Headers**:

```
Authorization: Bearer {accessToken}
```

**Response (200 OK)**:

```json
{
  "success": true,
  "data": {
    "message": "로그아웃되었습니다",
    "logoutAt": "2025-11-09T10:30:00"
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
    "code": "AUTH003",
    "message": "인증이 필요합니다"
  }
}
```

---

## 🧪 테스트 시나리오

### 정상 흐름

1. 로그인 → Access Token + Refresh Token 발급
2. Access Token으로 로그아웃 요청
3. Refresh Token이 DB에서 삭제됨
4. 로그아웃 후 API 요청 → 401 에러 (Access Token 만료 후)
5. Refresh Token으로 갱신 시도 → 401 에러 (토큰 삭제됨)

### 엣지 케이스

1. 이미 로그아웃한 사용자가 다시 로그아웃 → 성공 (멱등성)
2. 인증 없이 로그아웃 시도 → 401 에러
3. 만료된 Access Token으로 로그아웃 → 401 에러

---

## 💡 보안 고려사항

### 1. Access Token 잔여 시간

- 로그아웃 후에도 Access Token은 만료 전까지 유효
- **해결책**: Access Token 유효기간을 짧게 설정 (1시간 권장)
- **고급**: Redis 블랙리스트 사용

### 2. Refresh Token 보안

- Refresh Token은 즉시 삭제되어 재사용 불가
- **보안**: Refresh Token Rotation으로 탈취 시 감지 가능

### 3. CSRF 보호

- POST 요청이므로 CSRF 공격 위험
- **해결**: JWT 사용 시 CSRF는 비활성화 (Stateless)

### 4. XSS 보호

- 토큰을 localStorage에 저장하면 XSS 위험
- **권장**: HttpOnly 쿠키 사용 (고급 구현)

---

## 🔄 전체 인증 흐름 정리

### 회원가입 → 로그인 → API 사용 → 로그아웃

```
1. 회원가입
   POST /api/auth/signup
   → User 생성

2. 로그인
   POST /api/auth/login
   → Access Token + Refresh Token 발급
   → Refresh Token DB 저장

3. API 사용
   GET /api/auth/me
   Header: Authorization: Bearer {accessToken}
   → JWT 필터가 토큰 검증
   → 인증 성공

4. Access Token 만료
   POST /api/auth/refresh
   Body: { "refreshToken": "..." }
   → 새로운 Access Token + Refresh Token 발급
   → Refresh Token DB 업데이트

5. 로그아웃
   POST /api/auth/logout
   Header: Authorization: Bearer {accessToken}
   → Refresh Token DB 삭제
   → SecurityContext 정리

6. 로그아웃 후 API 사용
   GET /api/auth/me
   → 401 Unauthorized (Access Token 만료 후)
   
   POST /api/auth/refresh
   → 401 Unauthorized (Refresh Token 삭제됨)
```

---

## 🎯 학습 포인트

### JWT의 특성 이해

- **Stateless**: 서버에 세션 저장 없음
- **Self-contained**: 토큰 자체에 정보 포함
- **무효화 불가**: 만료 전까지 유효 (블랙리스트로 해결 가능)

### 보안과 편의성의 균형

- **Access Token**: 짧은 유효기간 (보안)
- **Refresh Token**: 긴 유효기간 (편의성)
- **Rotation**: 보안 강화 (재사용 불가)

### 멱등성 (Idempotency)

- 같은 요청을 여러 번 해도 결과가 동일
- 로그아웃은 멱등성을 가져야 함

---

## 🔗 다음 단계

Phase 7이 완료되면 **JWT 인증/인가 기본 기능 완성**입니다! 🎉

**다음 Phase 8**에서는 **에러 처리 및 마무리**를 진행합니다.

### 추가 개선 사항 (선택)

- 이메일 인증 (회원가입 후)
- 비밀번호 찾기/재설정
- 소셜 로그인 (Google, Kakao 등)
- 다중 기기 로그인 관리
- 역할 기반 권한 관리 (RBAC)

### 프로덕션 배포 전 확인사항

- [ ] JWT Secret Key 환경변수로 분리
- [ ] HTTPS 적용
- [ ] Rate Limiting 설정
- [ ] 로그 모니터링 설정
- [ ] 데이터베이스 인덱스 최적화
- [ ] API 문서화 (Swagger)

---

## 📚 참고 자료

- [RFC 7519 - JWT](https://tools.ietf.org/html/rfc7519)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)

