---
apply: API Controller 및 DTO 작성 시
---

# Swagger/OpenAPI 문서화 규칙

## 1. 기본 원칙

- **모든 Public API는 Swagger 문서화 필수**
- `@Operation`, `@ApiResponse` 어노테이션 사용
- 한글로 명확한 설명 작성
- 예시 값(example) 포함
- 성공/실패 케이스 모두 문서화

---

## 2. Controller 클래스 문서화

### 2.1 기본 구조

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "사용자", description = "사용자 관리 API")
@RequiredArgsConstructor
public class UserController {
    // ...
}
```

**규칙**:
- `@Tag`: API 그룹 이름과 설명
- `name`: 간결한 카테고리명 (한글)
- `description`: 상세 설명

---

## 3. 메서드 문서화

### 3.1 기본 구조

```java
@Operation(
    summary = "회원가입",
    description = "이메일과 비밀번호로 신규 사용자를 등록합니다."
)
@ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "회원가입 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiResponse.class),
            examples = @ExampleObject(
                name = "회원가입 성공 예시",
                value = """
                    {
                      "success": true,
                      "data": {
                        "id": 1,
                        "email": "user@example.com",
                        "name": "홍길동",
                        "createdAt": "2025-01-30T10:00:00"
                      }
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (유효성 검증 실패)",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "유효성 검증 실패 예시",
                value = """
                    {
                      "success": false,
                      "error": {
                        "code": "INVALID_INPUT_VALUE",
                        "message": "이메일 형식이 올바르지 않습니다"
                      }
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "409",
        description = "리소스 충돌 (이메일 중복)",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "이메일 중복 예시",
                value = """
                    {
                      "success": false,
                      "error": {
                        "code": "U002",
                        "message": "이미 사용 중인 이메일입니다"
                      }
                    }
                    """
            )
        )
    )
})
@PostMapping("/signup")
public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
    @Valid @RequestBody SignUpRequest request
) {
    // ...
}
```

**규칙**:
- `summary`: 간결한 제목 (1줄)
- `description`: 상세 설명 (여러 줄 가능)
- `@ApiResponses`: 모든 가능한 HTTP 응답 코드 문서화
- `examples`: JSON 예시 포함 (실제 응답과 동일하게)

---

## 4. DTO 문서화

### 4.1 Request DTO

```java
@Schema(description = "회원가입 요청")
public record SignUpRequest(

    @Schema(
        description = "이메일 주소",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    String email,

    @Schema(
        description = "비밀번호 (8자 이상)",
        example = "password123!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    String password,

    @Schema(
        description = "사용자 이름",
        example = "홍길동",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이름은 필수입니다")
    String name
) {
}
```

**규칙**:
- 클래스 레벨: `@Schema(description = "...")`
- 필드 레벨:
  - `description`: 필드 설명
  - `example`: 예시 값 (실제 사용 가능한 값)
  - `requiredMode`: 필수 여부 명시

### 4.2 Response DTO

```java
@Schema(description = "회원가입 응답")
public record SignUpResponse(

    @Schema(description = "사용자 ID", example = "1")
    Long id,

    @Schema(description = "이메일 주소", example = "user@example.com")
    String email,

    @Schema(description = "사용자 이름", example = "홍길동")
    String name,

    @Schema(
        description = "가입 일시",
        example = "2025-01-30T10:00:00",
        type = "string",
        format = "date-time"
    )
    LocalDateTime createdAt
) {
    public static SignUpResponse from(User user) {
        return new SignUpResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getCreatedAt()
        );
    }
}
```

**규칙**:
- 모든 필드에 `@Schema` 추가
- `example`: 실제 데이터 형식에 맞는 예시
- `LocalDateTime`: `type="string"`, `format="date-time"` 명시

---

## 5. 인증 API 문서화

### 5.1 JWT 인증이 필요한 API

```java
@Operation(
    summary = "사용자 정보 조회",
    description = "현재 로그인한 사용자의 정보를 조회합니다.",
    security = @SecurityRequirement(name = "bearerAuth")
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공"
    ),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패 (토큰 없음 또는 만료)",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                    {
                      "success": false,
                      "error": {
                        "code": "UNAUTHORIZED",
                        "message": "인증이 필요합니다"
                      }
                    }
                    """
            )
        )
    )
})
@GetMapping("/me")
public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
    // ...
}
```

**규칙**:
- `security = @SecurityRequirement(name = "bearerAuth")` 추가
- 401, 403 응답 문서화

---

## 6. OpenAPI 설정

### 6.1 Configuration 클래스

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OneDay API")
                        .version("v1.0")
                        .description("OneDay 프로젝트 REST API 문서"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요 (Bearer 제외)")
                        )
                );
    }
}
```

### 6.2 application.yml 설정

```yaml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
    display-request-duration: true
    doc-expansion: none
```

---

## 7. HTTP 상태 코드 가이드

| 코드 | 설명 | 사용 시점 |
|------|------|----------|
| 200 | OK | 조회, 수정 성공 |
| 201 | Created | 생성 성공 |
| 204 | No Content | 삭제 성공 |
| 400 | Bad Request | 유효성 검증 실패 |
| 401 | Unauthorized | 인증 실패 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스 없음 |
| 409 | Conflict | 리소스 충돌 (중복) |
| 500 | Internal Server Error | 서버 오류 |

---

## 8. 체크리스트

### Controller
- [ ] `@Tag`로 API 그룹 설명
- [ ] 모든 Public 메서드에 `@Operation`
- [ ] 성공/실패 응답 모두 `@ApiResponses` 작성
- [ ] 예시 JSON 포함
- [ ] 인증 필요 시 `@SecurityRequirement` 추가

### DTO
- [ ] 클래스 레벨 `@Schema` 설명
- [ ] 모든 필드에 `@Schema` 설명
- [ ] `example` 값 제공
- [ ] `requiredMode` 명시
- [ ] LocalDateTime은 `type`, `format` 명시

### 테스트
- [ ] Swagger UI 접속 확인 (`/swagger-ui.html`)
- [ ] API 문서 생성 확인 (`/api-docs`)
- [ ] 예시 JSON 테스트 실행 가능 확인

---

## 9. 접속 URL

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

---

## 10. 참고 자료

- Springdoc OpenAPI: https://springdoc.org/
- OpenAPI Specification: https://swagger.io/specification/
- Swagger UI: https://swagger.io/tools/swagger-ui/

