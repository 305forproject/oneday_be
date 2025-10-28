---
apply: always
---

# 로깅 및 모니터링 가이드

## 1. 로깅 기본 원칙

### 1.1 로깅 도구

- **SLF4J + Logback** 사용 (Spring Boot 기본)

### 1.2 로깅 목적

- 애플리케이션 동작 추적
- 에러 원인 파악
- 성능 모니터링
- 보안 감사

## 2. 로그 레벨 사용 규칙

### 2.1 로그 레벨 정의

- **ERROR**: 심각한 오류, 즉시 조치 필요
- **WARN**: 잠재적 문제, 주의 필요
- **INFO**: 중요한 비즈니스 로직 실행 정보
- **DEBUG**: 개발 시 디버깅 정보
- **TRACE**: 매우 상세한 정보

### 2.2 로그 레벨별 사용 예시

#### ERROR

```java

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

	public void processPayment(Long orderId) {
		try {
			// 결제 처리
		} catch (PaymentException e) {
			log.error("결제 처리 실패 - OrderId: {}, Error: {}", orderId, e.getMessage(), e);
			throw e;
		}
	}
}
```

#### WARN

```java

@Service
@Slf4j
public class UserService {

	public void login(String email, String password) {
		Optional<User> user = userRepository.findByEmail(email);

		if (user.isEmpty()) {
			log.warn("존재하지 않는 사용자 로그인 시도 - Email: {}", email);
			throw new UserNotFoundException();
		}

		if (!passwordMatches(password, user.get().getPassword())) {
			log.warn("잘못된 비밀번호 로그인 시도 - Email: {}", email);
			throw new InvalidPasswordException();
		}
	}
}
```

#### INFO

```java

@Service
@Slf4j
public class OrderService {

	@Transactional
	public OrderResponse createOrder(OrderRequest request) {
		log.info("주문 생성 시작 - UserId: {}, ProductId: {}",
			request.getUserId(), request.getProductId());

		Order order = // 주문 생성 로직

			log.info("주문 생성 완료 - OrderId: {}, Amount: {}",
				order.getId(), order.getTotalAmount());

		return OrderResponse.from(order);
	}
}
```

#### DEBUG

```java

@Service
@Slf4j
public class UserService {

	public UserResponse findById(Long id) {
		log.debug("사용자 조회 시작 - UserId: {}", id);

		User user = userRepository.findById(id)
			.orElseThrow(() -> new UserNotFoundException());

		log.debug("사용자 조회 성공 - User: {}", user);

		return UserResponse.from(user);
	}
}
```

## 3. 로깅 작성 규칙

### 3.1 로그 메시지 형식

```java
// Good - 구조화된 메시지
log.info("주문 생성 완료 - OrderId: {}, UserId: {}, Amount: {}",
	orderId, userId, amount);

log.

error("결제 처리 실패 - OrderId: {}, Error: {}",orderId, e.getMessage(),e);

	// Bad - 비구조화된 메시지
	log.

info("주문 생성 완료: "+orderId +", "+userId);
log.

error("에러 발생: "+e.toString());
```

### 3.2 개인정보 마스킹

```java
// Good - 민감 정보 마스킹
log.info("사용자 로그인 - Email: {}****",email.substring(0, 3));
	log.

debug("비밀번호 변경 - UserId: {}",userId); // 비밀번호는 로깅하지 않음

// Bad - 민감 정보 노출
log.

info("사용자 로그인 - Email: {}, Password: {}",email, password);
log.

debug("카드 정보 - CardNumber: {}",cardNumber);
```

### 3.3 성능 고려

```java
// Good - 조건부 로깅
if(log.isDebugEnabled()){
	log.

debug("복잡한 연산 결과: {}",expensiveOperation());
	}

	// Good - 파라미터 바인딩
	log.

debug("사용자 조회 - UserId: {}",userId);

// Bad - 불필요한 문자열 연산
log.

debug("사용자 조회 - UserId: "+userId +", Name: "+getName());
```

## 4. Controller 로깅

### 4.1 요청/응답 로깅

```java

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

	private final UserService userService;

	@PostMapping
	public ResponseEntity<ApiResponse<UserResponse>> createUser(
		@Valid @RequestBody UserRequest request) {

		log.info("사용자 생성 요청 - Email: {}", request.getEmail());

		UserResponse response = userService.createUser(request);

		log.info("사용자 생성 완료 - UserId: {}", response.getId());

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
```

### 4.2 로깅 대상

- API 호출 시작/종료
- 요청 파라미터 (민감 정보 제외)
- 처리 시간 (필요시)

## 5. Service 로깅

### 5.1 비즈니스 로직 로깅

```java

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

	@Transactional
	public OrderResponse createOrder(Long userId, OrderRequest request) {
		log.info("주문 생성 시작 - UserId: {}, ProductId: {}",
			userId, request.getProductId());

		// 사용자 조회
		User user = userRepository.findById(userId)
			.orElseThrow(() -> {
				log.warn("사용자를 찾을 수 없음 - UserId: {}", userId);
				return new UserNotFoundException();
			});

		// 상품 재고 확인
		if (product.getStock() < request.getQuantity()) {
			log.warn("재고 부족 - ProductId: {}, 요청: {}, 재고: {}",
				product.getId(), request.getQuantity(), product.getStock());
			throw new InsufficientStockException();
		}

		// 주문 생성
		Order order = orderRepository.save(Order.builder()
			.user(user)
			.product(product)
			.quantity(request.getQuantity())
			.build());

		log.info("주문 생성 완료 - OrderId: {}, Amount: {}",
			order.getId(), order.getTotalAmount());

		return OrderResponse.from(order);
	}
}
```

### 5.2 로깅 대상

- 중요한 비즈니스 로직 실행 (INFO)
- 예외 상황 (WARN, ERROR)
- 트랜잭션 시작/종료 (필요시)

## 6. 예외 로깅

### 6.1 GlobalExceptionHandler에서 로깅

```java

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(
		BusinessException e, HttpServletRequest request) {

		log.warn("비즈니스 예외 발생 - Code: {}, Message: {}, URI: {}",
			e.getErrorCode().getCode(),
			e.getMessage(),
			request.getRequestURI());

		// ...
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(
		Exception e, HttpServletRequest request) {

		log.error("예상치 못한 예외 발생 - URI: {}, Method: {}",
			request.getRequestURI(),
			request.getMethod(),
			e);

		// ...
	}
}
```

### 6.2 예외 로깅 규칙

- 비즈니스 예외: WARN 레벨
- 시스템 예외: ERROR 레벨
- 스택 트레이스는 ERROR 레벨에만 포함

## 7. Logback 설정

### 7.1 application.yml 설정

```yaml
logging:
  level:
    root: INFO
    com.company.project: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 30
```

### 7.2 환경별 로그 레벨

```yaml
# application-local.yml
logging:
  level:
    com.company.project: DEBUG

# application-prod.yml
logging:
  level:
    com.company.project: INFO
```

## 8. 로깅 Best Practices

### 8.1 Do

- 구조화된 로그 메시지 사용
- 적절한 로그 레벨 사용
- 민감 정보 마스킹
- 의미 있는 컨텍스트 정보 포함

### 8.2 Don't

- 루프 안에서 과도한 로깅
- 민감 정보(비밀번호, 카드번호 등) 로깅
- 너무 상세한 정보를 INFO 레벨로 로깅
- Exception을 catch하고 로깅만 하고 다시 throw하지 않음

```java
// Bad
try{
someMethod();
}catch(
Exception e){
	log.

error("에러 발생",e);
// 예외를 삼킴 - 상위에서 처리 불가
}

	// Good
	try{

someMethod();
}catch(
Exception e){
	log.

error("에러 발생",e);
    throw e; // 또는 다른 예외로 감싸서 throw
}
```

