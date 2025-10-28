# API 설계 가이드

## 1. REST API 기본 원칙

### 1.1 리소스 중심 설계

- URL은 명사 사용
- 동사 사용 금지
- 복수형 사용 권장

```java
// Good
GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
,
// Bad
GET    /api/getUser
POST   /api/createUser
GET    /api/user  // 단수형
```

## 2. HTTP 메서드 사용 규칙

### 2.1 메서드별 용도

- **GET**: 조회 (안전, 멱등성)
- **POST**: 생성
- **PUT**: 전체 수정 (멱등성)
- **PATCH**: 부분 수정
- **DELETE**: 삭제 (멱등성)

### 2.2 예시

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.findAll();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // 생성
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // 전체 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

## 3. HTTP 상태 코드 사용 규칙

### 3.1 성공 응답

- **200 OK**: 조회, 수정, 삭제 성공
- **201 Created**: 생성 성공
- **204 No Content**: 성공했으나 반환할 데이터 없음

### 3.2 클라이언트 에러

- **400 Bad Request**: 잘못된 요청
- **401 Unauthorized**: 인증 실패
- **403 Forbidden**: 권한 없음
- **404 Not Found**: 리소스 없음
- **409 Conflict**: 중복 등 충돌

### 3.3 서버 에러

- **500 Internal Server Error**: 서버 내부 오류

## 4. 표준 응답 형식

### 4.1 성공 응답 구조

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동"
  },
  "error": null
}
```

### 4.2 실패 응답 구조

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "사용자를 찾을 수 없습니다.",
    "details": null
  }
}
```

### 4.3 응답 DTO 구현

```java
/**
 * API 공통 응답 형식
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorInfo error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorInfo(code, message, null));
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorInfo {
        private String code;
        private String message;
        private Object details;
    }
}
```

## 5. DTO 네이밍 컨벤션

### 5.1 Request DTO

- 접미사: **Request**
- 요청 데이터 전달용

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하여야 합니다.")
    private String name;
}
```

### 5.2 Response DTO

- 접미사: **Response**
- 응답 데이터 전달용

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
```

## 6. API 버전 관리

### 6.1 URI 기반 버전 관리 (권장)

```java
// v1
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {
    // ...
}

// v2
@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {
    // ...
}
```

## 7. 페이징 및 정렬

### 7.1 페이징 요청

```java
GET /api/users?page=0&size=20&sort=createdAt,desc
```

### 7.2 페이징 응답

```java
@GetMapping
public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sort) {
    
    Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
    Page<UserResponse> users = userService.findAll(pageable);
    return ResponseEntity.ok(ApiResponse.success(users));
}
```

## 8. 요청 검증

### 8.1 Validation 어노테이션 사용

```java
@Getter
@NoArgsConstructor
public class UserRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 20)
    private String name;

    @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
    @Max(value = 150, message = "나이는 150 이하여야 합니다.")
    private Integer age;
}
```

### 8.2 Controller에서 @Valid 사용

```java
@PostMapping
public ResponseEntity<ApiResponse<UserResponse>> createUser(
        @Valid @RequestBody UserRequest request) {
    UserResponse user = userService.createUser(request);
    return ResponseEntity.ok(ApiResponse.success(user));
}
```

