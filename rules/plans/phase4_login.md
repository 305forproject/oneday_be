# Phase 4: 로그인 기능 만들기 🔑

> **목표**: 이메일과 비밀번호로 로그인하면 JWT 토큰을 발급받습니다.

## 9단계: 로그인 데이터 형식 정의하기 (DTO)

**📌 왜 필요한가요?**

- 로그인 요청과 응답의 형식을 정의합니다
- 응답으로 JWT 토큰을 전달합니다

**📝 작업할 파일**:

- `src/main/java/com/oneday/core/dto/auth/LoginRequest.java`
- `src/main/java/com/oneday/core/dto/auth/LoginResponse.java`

---

## ✅ 9-1. LoginRequest 만들기 (로그인 요청)

```java
package com.oneday.core.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @Email(message = "이메일 형식이 올바르지 않습니다")
    @NotBlank(message = "이메일은 필수입니다")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
```

---

## ✅ 9-2. LoginResponse 만들기 (로그인 응답)

```java
package com.oneday.core.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    
    private String accessToken;     // 실제 API 요청에 사용할 토큰
    private String refreshToken;    // Access Token 갱신용 토큰
    private String tokenType;       // "Bearer" (고정값)
    private Long expiresIn;         // 토큰 유효 시간 (초 단위)
}
```

---

## 10단계: 로그인 비즈니스 로직 만들기 (Service)

**📌 왜 필요한가요?**

- 이메일/비밀번호를 확인하고 맞으면 토큰을 발급합니다
- 틀리면 에러를 발생시킵니다

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/service/auth/AuthServiceTest.java` (테스트 추가)
- `src/main/java/com/oneday/core/service/auth/AuthService.java` (구현 추가)

---

## ✅ 10-1. 테스트 추가 작성

```java
// AuthServiceTest.java에 추가

@Mock
private JwtTokenProvider jwtTokenProvider;

@Mock
private AuthenticationManager authenticationManager;

// 1. 올바른 정보로 로그인하면 토큰을 받는가?
@Test
void 로그인_성공() {
  // Given: 가입된 사용자가 있을 때
  String email = "test@example.com";
  String password = "password123";
  LoginRequest request = new LoginRequest(email, password);

  User user = User.builder()
    .id(1L)
    .email(email)
    .password("encodedPassword")
    .name("홍길동")
    .role(Role.USER)
    .build();

  given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
  given(jwtTokenProvider.generateAccessToken(email)).willReturn("access-token");
  given(jwtTokenProvider.generateRefreshToken(email)).willReturn("refresh-token");

  // When: 로그인하면
  LoginResponse response = authService.login(request);

  // Then: 토큰이 발급된다
  assertThat(response.getAccessToken()).isEqualTo("access-token");
  assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
  assertThat(response.getTokenType()).isEqualTo("Bearer");
  assertThat(response.getExpiresIn()).isEqualTo(3600L);
}

// 2. 비밀번호가 틀리면 에러가 나는가?
@Test
void 잘못된_비밀번호_예외_발생() {
  // Given: 가입된 사용자가 있지만 비밀번호가 틀릴 때
  LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

  given(authenticationManager.authenticate(any()))
    .willThrow(new BadCredentialsException("잘못된 비밀번호"));

  // When & Then: 로그인 시 예외가 발생한다
  assertThatThrownBy(() -> authService.login(request))
    .isInstanceOf(InvalidCredentialsException.class)
    .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
}

// 3. 없는 사용자로 로그인하면 에러가 나는가?
@Test
void 존재하지_않는_사용자_예외_발생() {
  // Given: 가입되지 않은 이메일로
  LoginRequest request = new LoginRequest("none@example.com", "password123");

  given(authenticationManager.authenticate(any()))
    .willThrow(new BadCredentialsException("사용자를 찾을 수 없음"));

  // When & Then: 로그인 시 예외가 발생한다
  assertThatThrownBy(() -> authService.login(request))
    .isInstanceOf(InvalidCredentialsException.class);
}

// 4. 발급받은 토큰이 실제로 유효한가?
@Test
void JWT_토큰_생성_검증() {
  // Given: 로그인을 했을 때
  String email = "test@example.com";
  LoginRequest request = new LoginRequest(email, "password123");

  User user = User.builder()
    .email(email)
    .password("encodedPassword")
    .name("홍길동")
    .role(Role.USER)
    .build();

  given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
  given(jwtTokenProvider.generateAccessToken(email)).willReturn("valid-token");
  given(jwtTokenProvider.generateRefreshToken(email)).willReturn("refresh-token");
  given(jwtTokenProvider.validateToken("valid-token")).willReturn(true);

  // When: 로그인해서 토큰을 받고
  LoginResponse response = authService.login(request);

  // Then: 토큰이 유효하다
  boolean isValid = jwtTokenProvider.validateToken(response.getAccessToken());
  assertThat(isValid).isTrue();
}
```

---

## ✅ 10-2. InvalidCredentialsException 만들기

```java
package com.oneday.core.exception.auth;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

public class InvalidCredentialsException extends CustomException {
    
    public InvalidCredentialsException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, message);
    }
    
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
```

---

## ✅ 10-3. AuthService에 login 메서드 추가

```java
// AuthService.java에 추가

private final JwtTokenProvider jwtTokenProvider;
private final AuthenticationManager authenticationManager;

/**
 * 로그인
 * @param request 로그인 요청 정보
 * @return JWT 토큰
 * @throws InvalidCredentialsException 이메일 또는 비밀번호가 틀린 경우
 */
@Transactional
public LoginResponse login(LoginRequest request) {
  log.info("로그인 시도: email={}", request.getEmail());

  try {
    // 1. 이메일/비밀번호 검증
    authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
        request.getEmail(),
        request.getPassword()
      )
    );
  } catch (AuthenticationException e) {
    log.warn("로그인 실패: email={}", request.getEmail());
    throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다");
  }

  // 2. 사용자 조회
  User user = userRepository.findByEmail(request.getEmail())
    .orElseThrow(() -> new InvalidCredentialsException("사용자를 찾을 수 없습니다"));

  // 3. JWT 토큰 생성
  String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
  String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

  log.info("로그인 성공: email={}", request.getEmail());

  // 4. 응답 반환
  return LoginResponse.builder()
    .accessToken(accessToken)
    .refreshToken(refreshToken)
    .tokenType("Bearer")
    .expiresIn(3600L)  // 1시간 (초 단위)
    .build();
}
```

**💡 용어 설명**:

- **AuthenticationManager**: Spring Security가 제공하는 인증 관리자
- **UsernamePasswordAuthenticationToken**: 사용자명/비밀번호 인증 토큰
- **InvalidCredentialsException**: 잘못된 자격증명(이메일/비밀번호) 예외

---

## 11단계: 로그인 API 만들기 (Controller)

**📌 왜 필요한가요?**

- `POST /api/auth/login` 주소로 로그인 요청을 받습니다

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/controller/auth/AuthControllerTest.java` (테스트 추가)
- `src/main/java/com/oneday/core/controller/auth/AuthController.java` (구현 추가)

---

## ✅ 11-1. Controller 테스트 추가

```java
// AuthControllerTest.java에 추가

@Test
void 로그인_API_성공() throws Exception {
    // Given: 로그인 요청
    LoginRequest request = new LoginRequest("test@example.com", "password123");
    
    LoginResponse response = LoginResponse.builder()
        .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        .refreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        .tokenType("Bearer")
        .expiresIn(3600L)
        .build();
    
    given(authService.login(any(LoginRequest.class))).willReturn(response);
    
    // When & Then: POST /api/auth/login 호출
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.data.expiresIn").value(3600));
}

@Test
void 로그인_인증_실패() throws Exception {
    // Given: 잘못된 비밀번호
    LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
    
    given(authService.login(any(LoginRequest.class)))
        .willThrow(new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다"));
    
    // When & Then: 401 Unauthorized 응답
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
}

@Test
void 로그인_유효성_검증_실패() throws Exception {
    // Given: 이메일 형식 오류
    LoginRequest request = new LoginRequest("잘못된이메일", "password123");
    
    // When & Then: 400 Bad Request 응답
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
}
```

---

## ✅ 11-2. AuthController에 login 메서드 추가

```java
// AuthController.java에 추가

/**
 * 로그인 API
 * @param request 로그인 요청 정보
 * @return JWT 토큰
 */
@PostMapping("/login")
public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request) {
    
    log.info("로그인 API 호출: email={}", request.getEmail());
    
    LoginResponse response = authService.login(request);
    
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

---

## ✅ Phase 4 체크리스트

- [ ] `LoginRequest.java` DTO 만들기
- [ ] `LoginResponse.java` DTO 만들기
- [ ] `InvalidCredentialsException.java` 만들기
- [ ] `AuthServiceTest.java`에 로그인 테스트 추가 (4개)
- [ ] `AuthService.java`에 login 메서드 구현
- [ ] 모든 테스트 실행 → ✅ 통과 확인
- [ ] `AuthControllerTest.java`에 로그인 테스트 추가 (3개)
- [ ] `AuthController.java`에 login 엔드포인트 추가
- [ ] 모든 테스트 실행 → ✅ 통과 확인
- [ ] Postman으로 실제 API 테스트

---

## 💡 Postman으로 테스트하기

### 1. 먼저 회원가입

```
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

### 2. 로그인

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

### 예상 응답 (200 OK)

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjk5MDAwMDAwLCJleHAiOjE2OTkwMDM2MDB9.xxxxx",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjk5MDAwMDAwLCJleHAiOjE2OTk2MDQ4MDB9.xxxxx",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "error": null
}
```

### 3. 토큰을 복사해서 저장하세요!

- 다음 단계에서 이 토큰을 사용해 API를 호출합니다

---

## 💡 JWT 토큰 확인하기

[jwt.io](https://jwt.io)에 접속해서 발급받은 토큰을 붙여넣으면 내용을 확인할 수 있습니다.

**토큰 구조**:

```
헤더.페이로드.서명

예:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9  // 헤더
.
eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjk5MDAwMDAwLCJleHAiOjE2OTkwMDM2MDB9  // 페이로드
.
xxxxx  // 서명
```

**페이로드 내용**:

```json
{
  "sub": "test@example.com",  // 사용자 이메일
  "iat": 1699000000,          // 발급 시간
  "exp": 1699003600           // 만료 시간
}
```

---

## 다음 단계

✅ Phase 4 완료 후 → **[Phase 5: JWT 필터와 보안 설정](phase5_security.md)** 로 이동하세요!

