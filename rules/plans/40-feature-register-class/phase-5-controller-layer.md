# Phase 5: Controller 계층

## 🎯 목표

클래스 등록 REST API 엔드포인트를 구현하고 Spring Security 설정을 추가합니다.

**예상 소요 시간**: 1시간

---

## ✅ 작업 체크리스트

### 1. ClassController 생성

- [ ] `controller.classes` 패키지 생성
- [ ] `ClassController.java` 클래스 생성
- [ ] ClassService 의존성 주입
- [ ] POST /api/classes 엔드포인트 구현
- [ ] Swagger 문서화 추가

### 2. SecurityConfig 수정

- [ ] `/api/classes/**` 경로 인증 설정 추가
- [ ] INSTRUCTOR 권한 검증

---

## 📝 파일 생성/수정 목록

### 생성할 파일 (1개)

1. `src/main/java/com/oneday/core/controller/classes/ClassController.java`

### 수정할 파일 (1개)

1. `src/main/java/com/oneday/core/config/security/SecurityConfig.java`

---

## 🔧 구현 가이드

### 1. ClassController

**파일**: `src/main/java/com/oneday/core/controller/classes/ClassController.java`

```java
package com.oneday.core.controller.classes;

import com.oneday.core.dto.classes.CreateClassRequest;
import com.oneday.core.dto.classes.CreateClassResponse;
import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.entity.User;
import com.oneday.core.service.classes.ClassService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 클래스 관련 API 컨트롤러
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Slf4j
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Tag(name = "클래스 API", description = "클래스 등록 및 관리 API")
public class ClassController {

  private final ClassService classService;

  /**
   * 클래스 등록
   *
   * @param user    인증된 강사 (INSTRUCTOR)
   * @param request 클래스 등록 요청 정보
   * @return 등록된 클래스 정보
   */
  @PostMapping
  @PreAuthorize("hasRole('INSTRUCTOR')")
  @Operation(
    summary = "클래스 등록",
    description = "강사(INSTRUCTOR)가 원데이 클래스를 등록합니다.",
    security = @SecurityRequirement(name = "Bearer Authentication")
  )
  public ResponseEntity<ApiResponse<CreateClassResponse>> createClass(
    @AuthenticationPrincipal User user,
    @Valid @RequestBody CreateClassRequest request
  ) {
    log.info("클래스 등록 API 호출: instructor={}, className={}",
      user.getEmail(), request.className());

    CreateClassResponse response = classService.createClass(user, request);

    log.info("클래스 등록 API 완료: classId={}", response.classId());

    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(ApiResponse.success(response));
  }
}
```

**구현 포인트**:

- `@RestController` + `@RequestMapping("/api/classes")`
- `@PreAuthorize("hasRole('INSTRUCTOR')")`: INSTRUCTOR 권한 검증
- `@AuthenticationPrincipal User user`: 인증된 사용자 정보 추출
- `@Valid`: Request DTO 자동 검증
- `HttpStatus.CREATED` (201): 리소스 생성 성공
- `ApiResponse.success()`: 공통 응답 래퍼 사용
- Swagger 문서화:
  - `@Tag`: API 그룹 설정
  - `@Operation`: 엔드포인트 설명
  - `@SecurityRequirement`: JWT 인증 필요 표시

---

### 2. SecurityConfig 수정

**파일**: `src/main/java/com/oneday/core/config/security/SecurityConfig.java`

**수정 위치**: `securityFilterChain()` 메서드의 `authorizeHttpRequests` 부분

```java

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
  http
    .csrf(AbstractHttpConfigurer::disable)
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth
      // 공개 엔드포인트
      .requestMatchers("/api/auth/**").permitAll()
      .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

      // 클래스 등록 엔드포인트 (INSTRUCTOR만)
      .requestMatchers(HttpMethod.POST, "/api/classes").hasRole("INSTRUCTOR")

      // 나머지는 인증 필요
      .anyRequest().authenticated()
    )
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

  return http.build();
}
```

**구현 포인트**:

- `HttpMethod.POST` 명시: POST 요청만 제한
- `hasRole("INSTRUCTOR")`: INSTRUCTOR 권한 필요
- 순서 중요: 구체적인 패턴을 먼저 배치
- YAGNI 원칙: 클래스 조회 등 다른 엔드포인트는 향후 추가

---

## 🧪 검증 방법

### 1. 애플리케이션 시작 확인

```bash
./gradlew bootRun
```

**예상 로그**:

```
Mapped "{[POST] /api/classes}" onto ClassController.createClass(...)
```

### 2. Swagger UI 확인

브라우저에서 접속:

```
http://localhost:8080/swagger-ui/index.html
```

**확인 사항**:

- "클래스 API" 그룹 존재
- "클래스 등록" 엔드포인트 문서화
- Request/Response 스키마 표시
- "🔒 Authorize" 버튼으로 JWT 토큰 설정 가능

### 3. 인증 없이 요청 (실패 테스트)

```bash
curl -X POST http://localhost:8080/api/classes \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "className": "테스트",
    "price": 50000,
    "description": "설명",
    "times": [...],
    "images": [...]
  }'
```

**예상 응답**: `401 Unauthorized`

### 4. USER 권한으로 요청 (실패 테스트)

```bash
# USER 권한 JWT 토큰 사용
curl -X POST http://localhost:8080/api/classes \
  -H "Authorization: Bearer {USER_JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

**예상 응답**: `403 Forbidden`

### 5. INSTRUCTOR 권한으로 요청 (성공 테스트)

```bash
# INSTRUCTOR 권한 JWT 토큰 사용
curl -X POST http://localhost:8080/api/classes \
  -H "Authorization: Bearer {INSTRUCTOR_JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "className": "요가 입문 클래스",
    "price": 50000,
    "description": "초보자를 위한 요가 클래스입니다",
    "times": [
      {
        "startTime": "2025-02-01T10:00:00",
        "endTime": "2025-02-01T12:00:00"
      }
    ],
    "images": [
      {
        "imageUrl": "https://example.com/image1.jpg"
      }
    ]
  }'
```

**예상 응답**: `201 Created`

```json
{
  "success": true,
  "data": {
    "classId": 1,
    "className": "요가 입문 클래스",
    "categoryName": "건강/뷰티",
    "price": 50000,
    "description": "초보자를 위한 요가 클래스입니다",
    "instructorName": "강사명",
    "times": [
      {
        "startTime": "2025-02-01T10:00:00",
        "endTime": "2025-02-01T12:00:00"
      }
    ],
    "imageUrls": [
      "https://example.com/image1.jpg"
    ],
    "createdAt": "2025-01-26T14:30:00"
  },
  "error": null
}
```

### 6. 검증 실패 테스트

```bash
# 빈 클래스명
curl -X POST http://localhost:8080/api/classes \
  -H "Authorization: Bearer {INSTRUCTOR_JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "className": "",  # 빈 값
    "price": 50000,
    ...
  }'
```

**예상 응답**: `400 Bad Request`

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "COMMON001",
    "message": "클래스명은 필수입니다"
  }
}
```

---

## 📋 API 명세

### POST /api/classes

**요청**:

```http
POST /api/classes HTTP/1.1
Host: localhost:8080
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "categoryId": 1,
  "className": "요가 입문 클래스",
  "price": 50000,
  "description": "초보자를 위한 요가 클래스입니다",
  "times": [
    {
      "startTime": "2025-02-01T10:00:00",
      "endTime": "2025-02-01T12:00:00"
    }
  ],
  "images": [
    {
      "imageUrl": "https://example.com/image1.jpg"
    }
  ]
}
```

**응답 (성공)**:

```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "success": true,
  "data": {
    "classId": 1,
    "className": "요가 입문 클래스",
    "categoryName": "건강/뷰티",
    "price": 50000,
    "description": "초보자를 위한 요가 클래스입니다",
    "instructorName": "강사명",
    "times": [
      {
        "startTime": "2025-02-01T10:00:00",
        "endTime": "2025-02-01T12:00:00"
      }
    ],
    "imageUrls": ["https://example.com/image1.jpg"],
    "createdAt": "2025-01-26T14:30:00"
  },
  "error": null
}
```

**에러 응답**:

- `400 Bad Request`: 입력값 검증 실패
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: 권한 부족 (INSTRUCTOR 아님)
- `404 Not Found`: 카테고리 미존재 (CL001)
- `409 Conflict`: 시간 중복 (CL003)

---

## ⚠️ 주의사항

1. **권한 검증**
  - `@PreAuthorize("hasRole('INSTRUCTOR')")` 필수
  - SecurityConfig에서 경로 설정과 중복 체크

2. **인증된 사용자 추출**
  - `@AuthenticationPrincipal User user`로 현재 사용자 정보 자동 주입
  - JWT 토큰에서 추출한 사용자 정보

3. **HTTP 상태 코드**
  - `201 Created`: 리소스 생성 성공
  - `400 Bad Request`: 검증 실패
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 권한 부족

4. **로깅**
  - API 진입점과 종료점에 로그 남김
  - `log.info` 사용

5. **Swagger 문서화**
  - `@Tag`, `@Operation`, `@SecurityRequirement` 필수
  - 클라이언트 개발자를 위한 명확한 설명

---

## 🔍 체크리스트

### 완료 조건

- [ ] ClassController 생성 완료
- [ ] POST /api/classes 엔드포인트 구현 완료
- [ ] SecurityConfig 수정 완료
- [ ] 컴파일 에러 없음
- [ ] 애플리케이션 정상 시작
- [ ] Swagger UI에서 API 문서 확인
- [ ] 인증 없이 요청 시 401 응답
- [ ] USER 권한으로 요청 시 403 응답
- [ ] INSTRUCTOR 권한으로 요청 시 201 응답
- [ ] 검증 실패 시 400 응답

---

## 🚀 다음 단계

Phase 6: 테스트 작성으로 진행

**다음 작업**: `rules/plans/40-feature-register-class/phase-6-testing.md` 참고

---

## 📊 진행 상황

- [x] Phase 5 작업 계획 수립
- [ ] Phase 5 구현 시작
- [ ] Phase 5 구현 완료
- [ ] Phase 5 검증 완료

