---
apply: always
---

# 코드 스타일 가이드

## 1. 기본 원칙

- Google Java Style Guide 기반
- Naver 포맷팅 적용
- Java 21 기준

## 2. 네이밍 컨벤션

### 2.1 클래스명

- **파스칼케이스(PascalCase)** 사용
- 명사 또는 명사구 사용

```java
// Good
public class UserService {
}

public class OrderController {
}

public class ProductRepository {
}

// Bad
public class userService {
}

public class order_controller {
}
```

### 2.2 메서드명

- **카멜케이스(camelCase)** 사용
- 동사 또는 동사구로 시작

```java
// Good
public User findUserById(Long id) {
}

public void createOrder() {
}

public boolean isActive() {
}

// Bad
public User FindUserById(Long id) {
}

public void CreateOrder() {
}

public boolean active() {
}
```

### 2.3 변수명

- **카멜케이스(camelCase)** 사용
- 의미 있는 이름 사용

```java
// Good
private String userName;
private List<Order> orderList;
private int totalCount;

// Bad
private String user_name;
private List<Order> list;
private int cnt;
```

### 2.4 상수명

- **UPPER_SNAKE_CASE** 사용
- static final 키워드와 함께 사용

```java
// Good
public static final int MAX_RETRY_COUNT = 3;
public static final String DEFAULT_ENCODING = "UTF-8";

// Bad
public static final int maxRetryCount = 3;
public static final String defaultEncoding = "UTF-8";
```

### 2.5 패키지명

- **소문자만** 사용
- 언더스코어(_) 사용 금지

```java
// Good
package com.company.project.service.user;
package com.company.project.controller.order;

// Bad
package com.company.project.Service.User;
package com.company.project.controller.Order_Management;
```

## 3. 포맷팅

### 3.1 들여쓰기

- 탭 대신 **스페이스 4칸** 사용
- 중괄호는 K&R 스타일 사용

```java
// Good
public class Example {
	public void method() {
		if (condition) {
			doSomething();
		}
	}
}

// Bad
public class Example {
	public void method() {
		if (condition) {
			doSomething();
		}
	}
}
```

### 3.2 줄 길이

- 최대 **100자** (Google Java Style Guide 기준)
- 긴 메서드 체인은 적절히 줄바꿈

```java
// Good
User user = userRepository.findById(id)
		.orElseThrow(() -> new UserNotFoundException("User not found"));

// Bad
User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
```

### 3.3 공백

- 이항 연산자 앞뒤로 공백
- 콤마 뒤에 공백
- 메서드명과 여는 괄호 사이에 공백 없음

```java
// Good
int result = a + b;

method(param1, param2, param3);

// Bad
int result = a + b;

method(param1, param2, param3);
```

## 4. import 문

- 와일드카드(*) 사용 지양
- 사용하지 않는 import 제거
- 자동 정렬 사용

```java
// Good

import java.util.List;
import java.util.ArrayList;

// Bad
import java.util.*;
```

## 5. 주석 및 Javadoc

### 5.1 클래스 레벨 Javadoc (필수)

```java
/**
 * 사용자 관리 서비스
 * 사용자 생성, 조회, 수정, 삭제 기능을 제공합니다.
 *
 * @author 작성자명
 * @since 2025-10-26
 */
public class UserService {
	// ...
}
```

### 5.2 메서드 레벨 주석

- 복잡한 로직에만 작성
- 간단한 코드는 주석 생략 가능

```java
// Good - 복잡한 로직
/**
 * 사용자 권한을 검증하고 주문을 생성합니다.
 * 재고가 부족한 경우 예외를 발생시킵니다.
 *
 * @param userId 사용자 ID
 * @param orderRequest 주문 요청 정보
 * @return 생성된 주문
 * @throws InsufficientStockException 재고 부족 시
 */
public Order createOrder(Long userId, OrderRequest orderRequest) {
	// 복잡한 비즈니스 로직
}

// Good - 간단한 로직
public User findById(Long id) {
	return userRepository.findById(id)
		.orElseThrow(() -> new UserNotFoundException("User not found"));
}
```

### 5.3 인라인 주석

- 명확하지 않은 코드에만 작성
- 코드 자체가 설명이 되도록 작성 권장

```java
// Good
// 관리자 권한이 있는 경우에만 전체 주문 조회 가능
if(user.hasRole(Role.ADMIN)){
	return orderRepository.

findAll();
}

	// Bad - 불필요한 주석
	// user를 저장한다
	userRepository.

save(user);
```

## 6. Lombok 사용 가이드

### 6.1 허용되는 어노테이션

```java
// Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User {
	// ...
}

// DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
	// ...
}

// Service
@RequiredArgsConstructor
@Service
public class UserService {
	private final UserRepository userRepository;
	// ...
}
```

### 6.2 지양하는 어노테이션

- `@Data`: 너무 많은 기능 포함, 명시적 사용 권장
- `@ToString`: Entity에서 사용 시 순환 참조 위험

```java
// Bad
@Data
public class User {
}

// Good
@Getter
@Setter
public class User {
}
```

## 7. 요구사항 기반 개발 원칙

### 7.1 YAGNI (You Aren't Gonna Need It)

- **요구사항에 명시된 기능만 구현**
- 예상, 예측, 망상으로 인한 추가 기능/코드 생성 지양
- "나중에 필요할 것 같다"는 이유로 코드 작성 금지

```java
// Bad - 요구사항에 없는 기능 추가
public class UserService {
	// 현재 요구사항: 사용자 조회만 필요
	public User findById(Long id) {
	}

	// 나중에 필요할 것 같아서 추가 (X)
	public List<User> findByAgeRange(int min, int max) {
	}

	public User findByEmailAndPhone(String email, String phone) {
	}
}

// Good - 요구사항에 있는 기능만
public class UserService {
	public User findById(Long id) {
	}
}
```

### 7.2 Over-Engineering 방지

- 현재 필요하지 않은 추상화 계층 생성 금지
- 단순한 기능을 복잡하게 만들지 않기

```java
// Bad - 불필요한 인터페이스와 추상화
public interface UserFinder {
}

public interface UserCreator {
}

public interface UserUpdater {
}

public class UserService implements UserFinder, UserCreator, UserUpdater {
}

// Good - 요구사항에 맞는 단순한 구조
public class UserService {
}
```

