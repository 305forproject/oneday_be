# Phase 3: 회원가입 기능 만들기 👤

> **목표**: 사용자가 이메일, 비밀번호, 이름을 입력해서 회원가입할 수 있게 합니다.

## 6단계: 회원가입 데이터 형식 정의하기 (DTO)

**📌 왜 필요한가요?**

- 사용자가 보내는 데이터의 형식을 정의해야 합니다
- 잘못된 데이터(예: 이메일 형식 오류)를 미리 걸러낼 수 있습니다

**📝 작업할 파일**:

- `src/main/java/com/oneday/core/dto/auth/SignUpRequest.java`
- `src/main/java/com/oneday/core/dto/auth/SignUpResponse.java`

---

## ✅ 6-1. SignUpRequest 만들기 (회원가입 요청)

```java
package com.oneday.core.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

  @Email(message = "이메일 형식이 올바르지 않습니다")
  @NotBlank(message = "이메일은 필수입니다")
  private String email;

  @NotBlank(message = "비밀번호는 필수입니다")
  @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
  private String password;

  @NotBlank(message = "이름은 필수입니다")
  private String name;
}
```

---

## ✅ 6-2. SignUpResponse 만들기 (회원가입 응답)

```java
package com.oneday.core.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SignUpResponse {

  private Long id;                      // 생성된 사용자 ID
  private String email;                 // 이메일
  private String name;                  // 이름
  private LocalDateTime createdAt;      // 가입 시간
}
```

**💡 용어 설명**:

- **DTO (Data Transfer Object)**: 데이터를 주고받기 위한 객체
- **@Valid**: 입력 데이터가 규칙을 지키는지 자동으로 검사
- **@Email**: 이메일 형식인지 검증
- **@NotBlank**: 빈 값이 아닌지 검증
- **@Size**: 문자열 길이 검증

---

## 7단계: 회원가입 비즈니스 로직 만들기 (Service)

**📌 왜 필요한가요?**

- 실제 회원가입 처리 로직(중복 확인, 비밀번호 암호화, 저장)을 작성합니다
- Controller는 요청만 받고, Service가 실제 일을 처리합니다

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/service/auth/AuthServiceTest.java` (테스트)
- `src/main/java/com/oneday/core/service/auth/AuthService.java` (구현)

---

## ✅ 7-1. 테스트 먼저 작성

```java
package com.oneday.core.service.auth;

import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.entity.User;
import com.oneday.core.exception.auth.DuplicateEmailException;
import com.oneday.core.repository.user.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthService authService;

  // 1. 정상적으로 회원가입이 되는가?
  @Test
  void 회원가입_성공() {
    // Given: 회원가입 요청이 있을 때
    SignUpRequest request = new SignUpRequest(
      "test@example.com",
      "password123",
      "홍길동"
    );

    given(userRepository.existsByEmail(anyString())).willReturn(false);
    given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
    given(userRepository.save(any(User.class))).willAnswer(invocation -> {
      User user = invocation.getArgument(0);
      // ID를 설정하여 저장된 것처럼 시뮬레이션
      return User.builder()
        .id(1L)
        .email(user.getEmail())
        .password(user.getPassword())
        .name(user.getName())
        .role(user.getRole())
        .build();
    });

    // When: 회원가입을 하면
    SignUpResponse response = authService.signUp(request);

    // Then: 사용자가 생성된다
    assertThat(response.getEmail()).isEqualTo("test@example.com");
    assertThat(response.getName()).isEqualTo("홍길동");
    assertThat(response.getId()).isNotNull();

    // 비밀번호 암호화가 호출되었는지 확인
    verify(passwordEncoder).encode("password123");
    // 저장이 호출되었는지 확인
    verify(userRepository).save(any(User.class));
  }

  // 2. 이미 가입된 이메일로 가입하면 에러가 나는가?
  @Test
  void 중복_이메일_예외_발생() {
    // Given: 이미 가입된 이메일이 있을 때
    SignUpRequest request = new SignUpRequest(
      "test@example.com",
      "password123",
      "홍길동"
    );

    given(userRepository.existsByEmail("test@example.com")).willReturn(true);

    // When & Then: 회원가입 시 예외가 발생한다
    assertThatThrownBy(() -> authService.signUp(request))
      .isInstanceOf(DuplicateEmailException.class)
      .hasMessageContaining("이미 사용 중인 이메일입니다");
  }

  // 3. 비밀번호가 암호화되어 저장되는가?
  @Test
  void 비밀번호_암호화_확인() {
    // Given: 회원가입 요청이 있을 때
    SignUpRequest request = new SignUpRequest(
      "test@example.com",
      "password123",
      "홍길동"
    );

    given(userRepository.existsByEmail(anyString())).willReturn(false);
    given(passwordEncoder.encode("password123")).willReturn("$2a$10$encodedPassword");
    given(userRepository.save(any(User.class))).willAnswer(invocation ->
      invocation.getArgument(0)
    );

    // When: 회원가입을 하면
    authService.signUp(request);

    // Then: 비밀번호 암호화가 호출된다
    verify(passwordEncoder).encode("password123");
  }
}
```

---

## ✅ 7-2. AuthService 구현

```java
package com.oneday.core.service.auth;

import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;
import com.oneday.core.exception.auth.DuplicateEmailException;
import com.oneday.core.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 회원가입
   * @param request 회원가입 요청 정보
   * @return 생성된 사용자 정보
   * @throws DuplicateEmailException 이메일이 이미 존재하는 경우
   */
  @Transactional
  public SignUpResponse signUp(SignUpRequest request) {
    log.info("회원가입 시도: email={}", request.getEmail());

    // 1. 이메일 중복 확인
    if (userRepository.existsByEmail(request.getEmail())) {
      log.warn("중복된 이메일로 회원가입 시도: {}", request.getEmail());
      throw new DuplicateEmailException("이미 사용 중인 이메일입니다");
    }

    // 2. 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    // 3. 사용자 생성 및 저장
    User user = User.builder()
      .email(request.getEmail())
      .password(encodedPassword)
      .name(request.getName())
      .role(Role.USER)
      .build();

    User savedUser = userRepository.save(user);
    log.info("회원가입 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());

    // 4. 응답 반환
    return SignUpResponse.builder()
      .id(savedUser.getId())
      .email(savedUser.getEmail())
      .name(savedUser.getName())
      .createdAt(savedUser.getCreatedAt())
      .build();
  }
}
```

---

## ✅ 7-3. DuplicateEmailException 만들기

```java
package com.oneday.core.exception.auth;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

public class DuplicateEmailException extends CustomException {
    
    public DuplicateEmailException(String message) {
        super(ErrorCode.DUPLICATE_EMAIL, message);
    }
    
    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }
}
```

**💡 용어 설명**:

- **PasswordEncoder**: 비밀번호를 안전하게 암호화하는 도구
- **BCrypt**: 비밀번호 암호화 알고리즘 (같은 비밀번호도 매번 다르게 암호화됨)
- **@Transactional**: 메서드 실행을 하나의 트랜잭션으로 묶음

---

## 8단계: 회원가입 API 만들기 (Controller)

**📌 왜 필요한가요?**

- 사용자가 실제로 접근할 수 있는 URL(엔드포인트)을 만들어야 합니다
- `POST /api/auth/signup` 주소로 회원가입 요청을 받습니다

**📝 작업할 파일**:

- `src/test/java/com/oneday/core/controller/auth/AuthControllerTest.java` (테스트)
- `src/main/java/com/oneday/core/controller/auth/AuthController.java` (구현)

---

## ✅ 8-1. Controller 테스트 작성

```java
package com.oneday.core.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.service.auth.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AuthService authService;

  // 1. 회원가입 API가 정상 작동하는가?
  @Test
  void 회원가입_API_성공() throws Exception {
    // Given: 회원가입 요청 데이터
    SignUpRequest request = new SignUpRequest(
      "test@example.com",
      "password123",
      "홍길동"
    );

    SignUpResponse response = SignUpResponse.builder()
      .id(1L)
      .email("test@example.com")
      .name("홍길동")
      .createdAt(LocalDateTime.now())
      .build();

    given(authService.signUp(any(SignUpRequest.class))).willReturn(response);

    // When & Then: POST /api/auth/signup 호출
    mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.data.email").value("test@example.com"))
      .andExpect(jsonPath("$.data.name").value("홍길동"));
  }

  // 2. 잘못된 데이터로 요청하면 에러가 나는가?
  @Test
  void 유효성_검증_실패_이메일_형식_오류() throws Exception {
    // Given: 이메일 형식이 잘못된 요청
    SignUpRequest request = new SignUpRequest(
      "잘못된이메일",
      "password123",
      "홍길동"
    );

    // When & Then: 400 Bad Request 응답
    mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest());
  }

  @Test
  void 유효성_검증_실패_비밀번호_짧음() throws Exception {
    // Given: 비밀번호가 8자 미만
    SignUpRequest request = new SignUpRequest(
      "test@example.com",
      "short",
      "홍길동"
    );

    // When & Then: 400 Bad Request 응답
    mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest());
  }
}
```

---

## ✅ 8-2. AuthController 구현

```java
package com.oneday.core.controller.auth;

import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.service.auth.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /**
   * 회원가입 API
   * @param request 회원가입 요청 정보
   * @return 생성된 사용자 정보
   */
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
    @Valid @RequestBody SignUpRequest request) {

    log.info("회원가입 API 호출: email={}", request.getEmail());

    SignUpResponse response = authService.signUp(request);

    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(ApiResponse.success(response));
  }
}
```

**💡 용어 설명**:

- **@RestController**: 이 클래스가 API를 처리한다는 표시
- **@PostMapping**: POST 방식의 요청을 받는다는 표시
- **@Valid**: 요청 데이터가 올바른지 자동으로 검사
- **MockMvc**: 실제 서버 없이 API를 테스트하는 도구
- **@WebMvcTest**: Controller 계층만 테스트

---

## ✅ Phase 3 체크리스트

- [ ] `SignUpRequest.java` DTO 만들기
- [ ] `SignUpResponse.java` DTO 만들기
- [ ] `DuplicateEmailException.java` 만들기
- [ ] `AuthServiceTest.java` 작성 (3개 테스트)
- [ ] `AuthService.java` 구현 (signUp 메서드)
- [ ] 모든 테스트 실행 → ✅ 통과 확인
- [ ] `AuthControllerTest.java` 작성 (3개 테스트)
- [ ] `AuthController.java` 구현 (signup 엔드포인트)
- [ ] 모든 테스트 실행 → ✅ 통과 확인
- [ ] Postman으로 실제 API 테스트

---

## 💡 Postman으로 테스트하기

### 회원가입 요청

```
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

### 예상 응답 (201 Created)

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "test@example.com",
    "name": "홍길동",
    "createdAt": "2025-11-03T10:30:00"
  },
  "error": null
}
```

---

## 다음 단계

✅ Phase 3 완료 후 → **[Phase 4: 로그인 기능](phase4_login.md)** 로 이동하세요!

