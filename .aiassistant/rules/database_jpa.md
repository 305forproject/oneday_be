---
apply: always
---

# 데이터베이스 및 JPA 가이드

## 1. 데이터베이스 기본 정보

### 1.1 사용 데이터베이스

- **MySQL** 사용
- Docker 컨테이너로 구성

## 2. Entity 설계 규칙

### 2.1 기본 Entity 구조

```java
/**
 * 사용자 엔티티
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false)
	private final boolean active = true;

	@Builder
	public User(String email, String name) {
		this.email = email;
		this.name = name;
	}
}
```

### 2.2 JPA 어노테이션 위치

- **필드 위에 직접 작성** (Field Access)
- Getter/Setter를 통한 접근보다 직접 필드 접근 권장

```java
// Good - 필드 레벨 어노테이션
@Entity
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String email;
}
```

### 2.3 기본 키 전략

- **GenerationType.IDENTITY** 사용 (MySQL AUTO_INCREMENT)

```java

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

## 3. Auditing 설정

### 3.1 BaseEntity 생성

```java
/**
 * 공통 Auditing 정보를 관리하는 기본 엔티티
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;
}
```

### 3.2 Entity에서 상속

```java

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String email;
	private String name;
}
```

### 3.3 JPA Auditing 활성화

```java

@Configuration
@EnableJpaAuditing
public class JpaConfig {
	// Auditing 활성화
}
```

## 4. Entity ↔ DTO 변환 규칙

### 4.1 Entity → DTO 변환

- **정적 팩토리 메서드** 사용: `from()`, `of()`
- Response DTO에 변환 로직 위치

```java

@Getter
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

### 4.2 DTO → Entity 변환

- **Builder 패턴** 또는 **생성자** 사용
- Service 계층에서 변환 수행

```java

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;

	@Transactional
	public UserResponse createUser(UserRequest request) {
		// DTO → Entity 변환
		User user = User.builder()
			.email(request.getEmail())
			.name(request.getName())
			.build();

		User savedUser = userRepository.save(user);

		// Entity → DTO 변환
		return UserResponse.from(savedUser);
	}
}
```

### 4.3 변환 위치 규칙

- **Controller**: Entity 직접 사용 금지, 항상 DTO 사용
- **Service**: Entity ↔ DTO 변환 수행
- **Repository**: Entity만 사용

## 5. 연관관계 매핑

### 5.1 @ManyToOne (다대일)

```java

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private final User user;

	@Builder
	public Order(User user) {
		this.user = user;
	}
}
```

### 5.2 FetchType 사용 규칙

- **기본값**: LAZY (지연 로딩) 사용
- 필요한 경우에만 EAGER 사용 (N+1 문제 주의)

```java
// Good - LAZY 로딩
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
```

## 6. 테이블 네이밍 규칙

### 6.1 테이블명

- **복수형** 사용 권장
- **스네이크 케이스** 사용

```java

@Entity
@Table(name = "users")
public class User {
}

@Entity
@Table(name = "order_items")
public class OrderItem {
}
```

### 6.2 컬럼명

- **스네이크 케이스** 사용

```java

@Column(name = "created_at")
private LocalDateTime createdAt;

@Column(name = "user_name")
private String userName;
```

## 7. Repository 작성 규칙

### 7.1 기본 Repository

```java
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	List<User> findByNameContaining(String name);
}
```

### 7.2 커스텀 쿼리

```java
public interface UserRepository extends JpaRepository<User, Long> {

	// JPQL 사용
	@Query("SELECT u FROM User u WHERE u.email = :email AND u.active = true")
	Optional<User> findActiveUserByEmail(@Param("email") String email);
}
```

## 8. 엔티티 설계 주의사항

### 8.1 Setter 사용 지양

- `@Setter` 사용 금지
- 불변성 유지를 위해 생성자 또는 Builder 사용
- 필요한 경우 명시적 메서드 작성

```java
// Good
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
	private String name;
	private boolean active;

	public void updateName(String name) {
		this.name = name;
	}

	public void activate() {
		this.active = true;
	}
}
```

## 9. 성능 최적화

### 9.1 N+1 문제 해결

```java
// Good - Fetch Join 사용
@Query("SELECT o FROM Order o JOIN FETCH o.user")
List<Order> findAllWithUser();
```

### 9.2 Batch Size 설정

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
```

