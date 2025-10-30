---
apply: always
---

# 예외 처리 가이드

## 1. 예외 처리 기본 원칙

### 1.1 예외 처리 전략

- **전역 예외 처리**: `@ControllerAdvice` 사용
- **커스텀 예외**: 비즈니스 예외는 커스텀 예외로 정의
- **명확한 에러 메시지**: 사용자가 이해할 수 있는 메시지 제공

## 2. 커스텀 예외 계층 구조

### 2.1 기본 커스텀 예외

```java
/**
 * 비즈니스 예외의 최상위 클래스
 */
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
```

### 2.2 에러 코드 enum

```java
/**
 * 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통 (4xx, 5xx)
	INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다."),
	UNAUTHORIZED(401, "C002", "인증이 필요합니다."),
	FORBIDDEN(403, "C003", "권한이 없습니다."),
	INTERNAL_SERVER_ERROR(500, "C004", "서버 내부 오류가 발생했습니다."),

	// 사용자 (4xx)
	USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
	DUPLICATE_EMAIL(409, "U002", "이미 존재하는 이메일입니다."),
	INVALID_PASSWORD(400, "U003", "비밀번호가 일치하지 않습니다."),

	// 주문 (4xx)
	ORDER_NOT_FOUND(404, "O001", "주문을 찾을 수 없습니다."),
	INSUFFICIENT_STOCK(400, "O002", "재고가 부족합니다."),
	ORDER_ALREADY_CANCELLED(409, "O003", "이미 취소된 주문입니다.");

	private final int status;
	private final String code;
	private final String message;
}
```

**HTTP 상태 코드 사용 가이드**:
- **400 Bad Request**: 클라이언트 입력 오류, 유효성 검증 실패
- **401 Unauthorized**: 인증 실패 (로그인 필요)
- **403 Forbidden**: 권한 없음 (인증은 되었으나 접근 권한 없음)
- **404 Not Found**: 리소스를 찾을 수 없음
- **409 Conflict**: 리소스 충돌 (중복, 상태 충돌)
- **500 Internal Server Error**: 서버 내부 오류

### 2.3 도메인별 커스텀 예외

```java
/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
public class UserNotFoundException extends BusinessException {

	public UserNotFoundException() {
		super(ErrorCode.USER_NOT_FOUND);
	}

	public UserNotFoundException(String message) {
		super(ErrorCode.USER_NOT_FOUND, message);
	}
}

/**
 * 이메일 중복 시 발생하는 예외
 */
public class DuplicateEmailException extends BusinessException {

	public DuplicateEmailException() {
		super(ErrorCode.DUPLICATE_EMAIL);
	}

	public DuplicateEmailException(String message) {
		super(ErrorCode.DUPLICATE_EMAIL, message);
	}
}
```

## 3. 전역 예외 처리

### 3.1 GlobalExceptionHandler

```java
/**
 * 전역 예외 처리 핸들러
 * 모든 예외를 catch하여 일관된 형식의 응답을 반환합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 비즈니스 예외 처리
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
		log.error("Business Exception: {}", e.getMessage());
		ErrorCode errorCode = e.getErrorCode();

		ApiResponse<Void> response = ApiResponse.error(
			errorCode.getCode(),
			e.getMessage()
		);

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(response);
	}

	/**
	 * Validation 예외 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationException(
		MethodArgumentNotValidException e) {
		log.error("Validation Exception: {}", e.getMessage());

		BindingResult bindingResult = e.getBindingResult();
		String errorMessage = bindingResult.getFieldErrors().stream()
			.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
			.collect(Collectors.joining(", "));

		ApiResponse<Void> response = ApiResponse.error(
			ErrorCode.INVALID_INPUT_VALUE.getCode(),
			errorMessage
		);

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(response);
	}

	/**
	 * 일반 예외 처리
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		log.error("Unexpected Exception: ", e);

		ApiResponse<Void> response = ApiResponse.error(
			ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
			ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
		);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(response);
	}
}
```

## 4. Service에서 예외 사용

### 4.1 예외 발생 예시

```java

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;

	public UserResponse findById(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new UserNotFoundException("ID: " + id + "인 사용자를 찾을 수 없습니다."));

		return UserResponse.from(user);
	}

	@Transactional
	public UserResponse createUser(UserRequest request) {
		// 이메일 중복 체크
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new DuplicateEmailException("이미 사용 중인 이메일입니다: " + request.getEmail());
		}

		User user = User.builder()
			.email(request.getEmail())
			.name(request.getName())
			.build();

		User savedUser = userRepository.save(user);
		return UserResponse.from(savedUser);
	}
}
```

## 5. 에러 응답 형식

### 5.1 표준 에러 응답

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "U001",
    "message": "사용자를 찾을 수 없습니다.",
    "details": null
  }
}
```

### 5.2 Validation 에러 응답

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "C001",
    "message": "email: 올바른 이메일 형식이 아닙니다., name: 이름은 필수입니다.",
    "details": null
  }
}
```

## 6. 예외 처리 Best Practices

### 6.1 구체적인 예외 사용

```java
// Good - 구체적인 예외
public User findById(Long id) {
	return userRepository.findById(id)
		.orElseThrow(() -> new UserNotFoundException("사용자 ID: " + id));
}

// Bad - 일반적인 예외
public User findById(Long id) {
	return userRepository.findById(id)
		.orElseThrow(() -> new RuntimeException("에러 발생"));
}
```

### 6.2 예외에 컨텍스트 정보 포함

```java
// Good
if(stock<orderQuantity){
	throw new

InsufficientStockException(
	String.format("재고 부족. 요청: %d, 현재: %d", orderQuantity, stock)
    );
		}

		// Bad
		if(stock<orderQuantity){
	throw new

InsufficientStockException("재고 부족");
}
```

## 7. 로깅 전략

### 7.1 로그 레벨 사용

```java

@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
	// 비즈니스 예외는 WARN 레벨
	log.warn("Business Exception - Code: {}, Message: {}",
		e.getErrorCode().getCode(), e.getMessage());
	// ...
}

@ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
	// 예상치 못한 예외는 ERROR 레벨
	log.error("Unexpected Exception", e);
	// ...
}
```

