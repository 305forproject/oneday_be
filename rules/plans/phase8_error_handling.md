# Phase 8: 에러 처리 및 마무리 🚨

> **목표**: 잘못된 요청이나 에러가 발생했을 때 사용자에게 명확한 메시지를 전달합니다.

## 20단계: 인증 관련 예외 클래스 만들기

**📌 왜 필요한가요?**

- 에러가 발생했을 때 무엇이 문제인지 명확하게 알려줘야 합니다
- 각 상황에 맞는 에러 코드와 메시지를 정의합니다

**📝 작업할 파일**:

- `src/main/java/com/oneday/core/exception/ErrorCode.java` (에러 코드 추가)
- `src/main/java/com/oneday/core/exception/auth/` (예외 클래스들)
- `src/main/java/com/oneday/core/exception/GlobalExceptionHandler.java` (예외 처리)

---

## ✅ 20-1. ErrorCode에 인증 관련 코드 추가

```java
package com.oneday.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // ...기존 코드...

  // 인증/인가 관련 (400번대)
  DUPLICATE_EMAIL(409, "AUTH001", "이미 사용 중인 이메일입니다"),
  INVALID_CREDENTIALS(401, "AUTH002", "이메일 또는 비밀번호가 올바르지 않습니다"),
  INVALID_TOKEN(401, "AUTH003", "유효하지 않은 토큰입니다"),
  EXPIRED_TOKEN(401, "AUTH004", "만료된 토큰입니다"),
  UNAUTHORIZED(401, "AUTH005", "인증이 필요합니다"),
  FORBIDDEN(403, "AUTH006", "접근 권한이 없습니다"),
  USER_NOT_FOUND(404, "AUTH007", "사용자를 찾을 수 없습니다");

  private final int status;
  private final String code;
  private final String message;
}
```

**💡 HTTP 상태 코드**:

- **401 Unauthorized**: 인증되지 않음 (로그인 필요)
- **403 Forbidden**: 권한 없음 (로그인은 했지만 접근 권한 없음)
- **404 Not Found**: 리소스를 찾을 수 없음
- **409 Conflict**: 중복 (이미 존재하는 데이터)

---

## ✅ 20-2. 커스텀 예외 클래스 만들기

### InvalidTokenException (잘못된 토큰)

```java
package com.oneday.core.exception.auth;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

public class InvalidTokenException extends CustomException {

  public InvalidTokenException(String message) {
    super(ErrorCode.INVALID_TOKEN, message);
  }

  public InvalidTokenException() {
    super(ErrorCode.INVALID_TOKEN);
  }
}
```

### ExpiredTokenException (만료된 토큰)

```java
package com.oneday.core.exception.auth;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

public class ExpiredTokenException extends CustomException {

  public ExpiredTokenException(String message) {
    super(ErrorCode.EXPIRED_TOKEN, message);
  }

  public ExpiredTokenException() {
    super(ErrorCode.EXPIRED_TOKEN);
  }
}
```

### UnauthorizedException (인증 필요)

```java
package com.oneday.core.exception.auth;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

public class UnauthorizedException extends CustomException {

  public UnauthorizedException(String message) {
    super(ErrorCode.UNAUTHORIZED, message);
  }

  public UnauthorizedException() {
    super(ErrorCode.UNAUTHORIZED);
  }
}
```

---

## ✅ 20-3. GlobalExceptionHandler에 인증 예외 처리 추가

```java
package com.oneday.core.exception;

import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.exception.auth.*;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 커스텀 예외 처리
   */
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
    log.error("CustomException: {}", e.getMessage());

    ErrorCode errorCode = e.getErrorCode();

    return ResponseEntity
      .status(errorCode.getStatus())
      .body(ApiResponse.error(errorCode, e.getMessage()));
  }

  /**
   * 로그인 실패 예외 처리
   */
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
    InvalidCredentialsException e) {

    log.warn("로그인 실패: {}", e.getMessage());

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
  }

  /**
   * 토큰 관련 예외 처리
   */
  @ExceptionHandler({InvalidTokenException.class, ExpiredTokenException.class})
  public ResponseEntity<ApiResponse<Void>> handleTokenException(CustomException e) {
    log.warn("토큰 오류: {}", e.getMessage());

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
  }

  /**
   * Spring Security 인증 예외 처리
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
    AuthenticationException e) {

    log.error("인증 실패: {}", e.getMessage());

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
  }

  /**
   * Spring Security 권한 예외 처리
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
    AccessDeniedException e) {

    log.error("접근 거부: {}", e.getMessage());

    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(ApiResponse.error(ErrorCode.FORBIDDEN));
  }

  /**
   * 유효성 검증 실패 예외 처리
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
    MethodArgumentNotValidException e) {

    log.warn("유효성 검증 실패");

    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError)error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ApiResponse.error(ErrorCode.INVALID_INPUT, errors));
  }

  /**
   * 그 외 모든 예외 처리
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("예상치 못한 오류 발생", e);

    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
  }
}
```

**💡 용어 설명**:

- **@RestControllerAdvice**: 모든 Controller의 예외를 처리하는 클래스
- **@ExceptionHandler**: 특정 예외를 처리하는 메서드
- **MethodArgumentNotValidException**: @Valid 검증 실패 시 발생하는 예외

---

## 21단계: 통합 테스트 작성하기

**📌 왜 필요한가요?**

- 각 기능이 따로따로는 잘 작동하지만, 전체가 함께 작동하는지 확인해야 합니다
- 실제 사용자 시나리오를 테스트합니다

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/integration/AuthIntegrationTest.java`

---

## ✅ 21-1. 통합 테스트 작성

```java
package com.oneday.core.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.oneday.core.dto.auth.LoginRequest;
import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.repository.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 인증 통합 테스트
 * 실제 애플리케이션처럼 전체 흐름을 테스트합니다
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  /**
   * 시나리오 1: 회원가입 → 로그인 → 인증된 API 호출
   */
  @Test
  void 회원가입_로그인_인증_성공_시나리오() throws Exception {
    // 1단계: 회원가입
    SignUpRequest signUpRequest = new SignUpRequest(
      "test@example.com",
      "password123",
      "홍길동"
    );

    mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(signUpRequest)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.email").value("test@example.com"));

    // 2단계: 로그인해서 토큰 받기
    LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");

    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.accessToken").exists())
      .andReturn();

    // 토큰 추출
    String responseBody = loginResult.getResponse().getContentAsString();
    String accessToken = JsonPath.parse(responseBody).read("$.data.accessToken");

    // 3단계: 토큰으로 보호된 API 호출
    mockMvc.perform(get("/api/users/me")
        .header("Authorization", "Bearer " + accessToken))
      .andExpect(status().isOk());
  }

  /**
   * 시나리오 2: 토큰 없이 보호된 API 호출 → 401
   */
  @Test
  void 토큰_없이_API_호출_시_401() throws Exception {
    mockMvc.perform(get("/api/users/me"))
      .andExpect(status().isUnauthorized());
  }

  /**
   * 시나리오 3: 잘못된 토큰으로 API 호출 → 401
   */
  @Test
  void 잘못된_토큰으로_API_호출_시_401() throws Exception {
    mockMvc.perform(get("/api/users/me")
        .header("Authorization", "Bearer invalid-token"))
      .andExpect(status().isUnauthorized());
  }

  /**
   * 시나리오 4: 중복 이메일로 회원가입 → 409
   */
  @Test
  void 중복_이메일_회원가입_실패() throws Exception {
    // 첫 번째 회원가입
    SignUpRequest request = new SignUpRequest(
      "test@example.com",
      "password123",
      "홍길동"
    );

    mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated());

    // 같은 이메일로 두 번째 회원가입 시도
    mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.success").value(false))
      .andExpect(jsonPath("$.error.code").value("AUTH001"));
  }

  /**
   * 시나리오 5: 잘못된 비밀번호로 로그인 → 401
   */
  @Test
  void 잘못된_비밀번호_로그인_실패() throws Exception {
    // 회원가입
    SignUpRequest signUpRequest = new SignUpRequest(
      "test@example.com",
      "password123",
      "홍길동"
    );

    mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(signUpRequest)))
      .andExpect(status().isCreated());

    // 잘못된 비밀번호로 로그인
    LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongpassword");

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.success").value(false))
      .andExpect(jsonPath("$.error.code").value("AUTH002"));
  }
}
```

**💡 용어 설명**:

- **@SpringBootTest**: 전체 애플리케이션 컨텍스트를 로드하여 테스트
- **@AutoConfigureMockMvc**: MockMvc 자동 설정
- **@Transactional**: 각 테스트 후 데이터베이스 롤백
- **통합 테스트**: 여러 컴포넌트가 함께 작동하는지 확인

---

## ✅ Phase 8 완료 체크리스트

### 기본 인증 예외 처리

- [ ] `ErrorCode.java`에 인증 관련 에러 코드 추가
  - AUTH001: 중복 이메일
  - AUTH002: 잘못된 인증 정보
  - AUTH003: 유효하지 않은 토큰
  - AUTH004: 만료된 토큰
  - AUTH005: 인증 필요
  - AUTH006: 접근 권한 없음
  - AUTH007: 사용자를 찾을 수 없음
- [ ] `InvalidTokenException.java` 만들기
- [ ] `ExpiredTokenException.java` 만들기
- [ ] `UnauthorizedException.java` 만들기

### Phase 6 (Refresh Token) 예외 처리

- [ ] `InvalidRefreshTokenException.java` 만들기
- [ ] `GlobalExceptionHandler`에 InvalidRefreshTokenException 핸들러 추가
- [ ] Refresh Token 만료 예외 처리
- [ ] Refresh Token 검증 실패 예외 처리

### Phase 7 (Logout) 예외 처리

- [ ] 로그아웃 시 인증 필요 예외 처리 확인
- [ ] 이미 로그아웃된 사용자 처리 (멱등성)

### 전역 예외 처리

- [ ] `GlobalExceptionHandler.java`에 인증 예외 처리 추가
- [ ] Spring Security 예외 처리 추가
- [ ] 유효성 검증 실패 예외 처리 추가
- [ ] 일반 예외 처리 추가

### 통합 테스트

- [ ] `AuthIntegrationTest.java` 작성
  - 회원가입 → 로그인 → 인증 API 호출 시나리오
  - 토큰 없이 API 호출 시나리오
  - 잘못된 토큰으로 API 호출 시나리오
  - 중복 이메일 회원가입 시나리오
  - 잘못된 비밀번호 로그인 시나리오
  - Refresh Token 갱신 시나리오
  - 로그아웃 후 토큰 갱신 시도 시나리오

### 최종 검증

- [ ] 모든 테스트 실행 → ✅ 통과 확인
- [ ] Postman으로 전체 시나리오 수동 테스트
- [ ] 코드 리뷰 및 리팩토링
- [ ] API 문서 업데이트

---

## 💡 전체 테스트 실행하기

### Gradle로 모든 테스트 실행

```bash
# Windows
gradlew test

# 테스트 결과 확인
# build/reports/tests/test/index.html 열기
```

### 테스트 커버리지 확인 (선택사항)

```bash
# JaCoCo 플러그인 추가 (build.gradle)
plugins {
    id 'jacoco'
}

# 커버리지 리포트 생성
gradlew test jacocoTestReport

# 리포트 확인
# build/reports/jacoco/test/html/index.html 열기
```

---

## 🎉 최종 검증

### 1. Postman으로 전체 시나리오 테스트

#### ✅ 회원가입

```
POST http://localhost:8080/api/auth/signup
{
  "email": "final@test.com",
  "password": "password123",
  "name": "최종테스트"
}
```

#### ✅ 로그인

```
POST http://localhost:8080/api/auth/login
{
  "email": "final@test.com",
  "password": "password123"
}
```

#### ✅ 토큰으로 보호된 API 호출

```
GET http://localhost:8080/api/users/me
Authorization: Bearer {토큰}
```

---

### 2. 에러 케이스 확인

#### ❌ 중복 이메일 (409)

```
POST http://localhost:8080/api/auth/signup
{
  "email": "final@test.com",  // 이미 가입된 이메일
  "password": "password123",
  "name": "중복테스트"
}
```

#### ❌ 잘못된 비밀번호 (401)

```
POST http://localhost:8080/api/auth/login
{
  "email": "final@test.com",
  "password": "wrongpassword"
}
```

#### ❌ 토큰 없이 접근 (401)

```
GET http://localhost:8080/api/users/me
```

#### ❌ 잘못된 토큰 (401)

```
GET http://localhost:8080/api/users/me
Authorization: Bearer invalid-token
```

---

## 🎓 추가 학습 자료

### 선택 기능 구현 (필요시)

- **Refresh Token**: [Phase 7-1: Refresh Token](phase7_refresh_token.md) (선택)
- **로그아웃**: [Phase 7-2: Logout](phase7_logout.md) (선택)
- **이메일 인증**: [Phase 7-3: Email Verification](phase7_email.md) (선택)

### 참고 문서

- **architecture.md**: 코드 구조화 방법
- **code_style.md**: 코딩 스타일 가이드
- **api_design.md**: API 설계 원칙
- **testing.md**: 테스트 작성 가이드
- **logging.md**: 로그 작성 방법

---

## 🎉 축하합니다!

**JWT 인증/인가 기능 개발을 완료했습니다!** 🎊

### 배운 내용

✅ JWT 토큰 생성과 검증  
✅ Spring Security 설정  
✅ 필터를 이용한 인증 처리  
✅ 에러 처리와 예외 관리  
✅ TDD 방식의 개발  
✅ 통합 테스트 작성

### 다음 단계

- 실제 프로젝트에 적용하기
- 추가 기능 구현하기
- 코드 리뷰 받기
- 성능 최적화하기

**화이팅! 🚀**



