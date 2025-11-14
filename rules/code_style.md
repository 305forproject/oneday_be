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

### 2.6 final 키워드 사용

#### Entity에서 final 사용 규칙

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    
    // ❌ JPA가 관리하는 필드는 final 사용 금지
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private String name;
    
    // ✅ 불변 필드는 final + 초기화
    @Column(nullable = false)
    private final boolean active = true;
    
    @Column(nullable = false, updatable = false)
    private final LocalDateTime registeredAt = LocalDateTime.now();
    
    @Builder
    public User(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
```

#### Service에서 final 사용

```java
@Service
@RequiredArgsConstructor
public class UserService {
    // ✅ 의존성 주입 필드는 final 사용
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
}
```

#### DTO에서 final 사용

```java
@Getter
public class UserResponse {
    // ✅ DTO는 불변 객체로 설계 (final 권장)
    private final Long id;
    private final String email;
    private final String name;
    
    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
    }
}
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

## 7. record 사용 가이드 (Java 16+)

### 7.1 record 사용 권장 대상

- **DTO (Request, Response)**
- **이벤트 객체**
- **설정 객체**
- **불변 데이터 모델**

```java
// ✅ DTO
public record UserRequest(
    @NotBlank String email,
    @NotBlank String password,
    @NotBlank String name
) {
}

public record UserResponse(
    Long id,
    String email,
    String name
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName()
        );
    }
}

// ✅ 이벤트
public record UserCreatedEvent(Long userId, String email, LocalDateTime createdAt) {
}

// ✅ 설정
public record JwtProperties(String secret, long expiration) {
}
```

### 7.2 record의 장점

- **불변성 자동 보장**: 모든 필드가 final
- **보일러플레이트 제거**: 생성자, getter, equals, hashCode, toString 자동 생성
- **코드 간결성**: 약 50% 코드 감소
- **명확한 의도**: "불변 데이터 전달용" 의미 명확
- **Java 표준**: Lombok 의존성 제거 가능

### 7.3 record 사용 시 주의사항

```java
// ✅ getter 메서드명: fieldName() 형태
UserResponse response = new UserResponse(1L, "test@example.com", "홍길동");
String email = response.email();    // getEmail() 아님
String name = response.name();      // getName() 아님

// ✅ Validation 어노테이션 사용 가능
public record UserRequest(
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email
) {
}

// ✅ Compact Constructor (검증 로직 추가)
public record UserRequest(String email, String name) {
    public UserRequest {
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("이름은 공백일 수 없습니다.");
        }
    }
}

// ❌ setter 없음: 불변 객체
// ❌ 상속 불가: final class로 생성됨
// ✅ interface 구현은 가능
```

### 7.4 record vs class 선택 기준

**DTO의 역할과 복잡도에 따라 유연하게 선택**

#### record 사용 조건 (✅)

- 단순 조회 응답
- 값이 변경되지 않음
- 간단한 변환만 필요
- 요청 데이터 (불변성 보장 필요)

```java
// ✅ 단순 CRUD 응답
public record UserResponse(Long id, String email, String name) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName()
        );
    }
}

// ✅ 요청 데이터
public record UserRequest(
    @NotBlank String email,
    @NotBlank String name
) {
}
```

#### class 사용 조건 (✅)

- 서비스 로직에서 값 변경 필요
- 복잡한 계산 로직 포함
- 여러 단계를 거쳐 데이터 수집
- 가변 상태 관리 필요

```java
// ✅ 복잡한 통계 응답 (여러 단계 데이터 수집)
@Getter
@Builder
public class UserStatisticsResponse {
    private final Long userId;
    private final String name;
    private final int totalPosts;
    private final int totalComments;
    private final double activityScore;
}

// ✅ 가변 상태가 필요한 경우
@Getter
public class ReportResponse {
    private final String reportName;
    private List<String> errors = new ArrayList<>();
    
    public void addError(String error) {
        this.errors.add(error);
    }
}

// ✅ Entity는 class 사용 (JPA 요구사항)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
}
```

#### 결정 기준 요약

| 상황       | 추천                | 이유          |
|----------|-------------------|-------------|
| 단순 조회 응답 | record ✅          | 불변성, 간결함    |
| 요청 데이터   | record ✅          | 검증 후 변경 불필요 |
| 값 변경 필요  | class ✅           | 가변 상태 관리    |
| 복잡한 계산   | class ✅           | 생성자 로직 복잡   |
| 여러 단계 수집 | class + Builder ✅ | 점진적 데이터 설정  |
| Entity   | class ✅           | JPA 요구사항    |

## 8. 요구사항 기반 개발 원칙

### 8.1 YAGNI (You Aren't Gonna Need It)

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

### 8.2 Over-Engineering 방지

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

