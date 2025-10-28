---
apply: always
---

# 아키텍처 및 프로젝트 구조 가이드

## 1. 전체 시스템 구조

### 1.1 멀티 모듈 구성

```
프로젝트
├── React FE 컨테이너 (프론트엔드)
├── Spring Boot BE 컨테이너 (백엔드 WAS)
├── MySQL 컨테이너 (데이터베이스)
└── 알림 서버 컨테이너 (예정)
```

## 2. 백엔드 패키지 구조

### 2.1 기본 원칙

- **레이어 우선 구조**: 레이어별로 패키지를 나눈 후 도메인별로 분리
- **MVC 패턴 Strict 적용**: Controller - Service - Repository 계층 엄격히 구분

### 2.2 패키지 구조 예시

```
com.company.project
├── controller
│   ├── user
│   │   └── UserController.java
│   ├── order
│   │   └── OrderController.java
│   └── product
│       └── ProductController.java
├── service
│   ├── user
│   │   └── UserService.java
│   ├── order
│   │   └── OrderService.java
│   └── product
│       └── ProductService.java
├── repository
│   ├── user
│   │   └── UserRepository.java
│   ├── order
│   │   └── OrderRepository.java
│   └── product
│       └── ProductRepository.java
├── entity
│   ├── User.java
│   ├── Order.java
│   └── Product.java
├── dto
│   ├── user
│   │   ├── UserRequest.java
│   │   └── UserResponse.java
│   ├── order
│   │   ├── OrderRequest.java
│   │   └── OrderResponse.java
│   └── common
│       ├── ApiResponse.java
│       └── ErrorResponse.java
├── exception
│   ├── CustomException.java
│   ├── UserNotFoundException.java
│   └── GlobalExceptionHandler.java
├── config
│   ├── WebConfig.java
│   └── JpaConfig.java
└── util
    └── DateUtil.java
```

## 3. 레이어별 역할 및 책임

### 3.1 Controller Layer

**역할**: HTTP 요청/응답 처리만 담당

**책임**:

- 요청 파라미터 바인딩
- DTO 검증 (Validation)
- Service 호출
- HTTP 응답 반환

**금지사항**:

- 비즈니스 로직 작성 금지
- 데이터베이스 직접 접근 금지
- 복잡한 연산 금지

```java
/**
 * 사용자 관리 컨트롤러
 * HTTP 요청을 받아 Service로 위임하고 응답을 반환합니다.
 *
 * @author 작성자명
 * @since 2025-10-26
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
		UserResponse user = userService.findById(id);
		return ResponseEntity.ok(ApiResponse.success(user));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<UserResponse>> createUser(
		@Valid @RequestBody UserRequest request) {
		UserResponse user = userService.createUser(request);
		return ResponseEntity.ok(ApiResponse.success(user));
	}
}
```

### 3.2 Service Layer

**역할**: 모든 비즈니스 로직 처리

**책임**:

- 비즈니스 로직 구현
- 트랜잭션 관리
- Repository 호출
- Entity ↔ DTO 변환
- 예외 처리

**원칙**:

- 하나의 Service는 하나의 도메인 담당
- 복잡한 로직은 private 메서드로 분리

```java
/**
 * 사용자 관리 서비스
 * 사용자 생성, 조회, 수정, 삭제 비즈니스 로직을 처리합니다.
 *
 * @author 작성자명
 * @since 2025-10-26
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;

	public UserResponse findById(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
		return UserResponse.from(user);
	}

	@Transactional
	public UserResponse createUser(UserRequest request) {
		// 중복 검사
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new DuplicateEmailException("이미 존재하는 이메일입니다.");
		}

		// Entity 생성 및 저장
		User user = User.builder()
			.email(request.getEmail())
			.name(request.getName())
			.build();

		User savedUser = userRepository.save(user);
		return UserResponse.from(savedUser);
	}

	@Transactional
	public void deleteUser(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

		userRepository.delete(user);
	}
}
```

### 3.3 Repository Layer

**역할**: 데이터베이스 접근만 담당

**책임**:

- CRUD 작업
- 커스텀 쿼리 정의
- JPA/Hibernate 활용

**원칙**:

- JpaRepository 상속
- 복잡한 쿼리는 @Query 사용
- 비즈니스 로직 포함 금지

```java
/**
 * 사용자 리포지토리
 *
 * @author 작성자명
 * @since 2025-10-26
 */
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	@Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
	List<User> findByNameContaining(@Param("name") String name);
}
```

## 4. 계층 간 데이터 흐름

```
Client Request
    ↓
Controller (HTTP 처리)
    ↓
Service (비즈니스 로직)
    ↓
Repository (데이터 접근)
    ↓
Database
```

## 5. 의존성 규칙

### 5.1 허용되는 의존성

- Controller → Service
- Service → Repository
- Service → Service (같은 레벨, 필요시만)

### 5.2 금지되는 의존성

- Controller → Repository (❌)
- Repository → Service (❌)
- Controller → Entity 직접 반환 (❌)

```java
// Bad - Controller에서 Repository 직접 호출
@RestController
public class UserController {
	private final UserRepository userRepository; // ❌
}

// Good - Service를 통해 접근
@RestController
public class UserController {
	private final UserService userService; // ✅
}
```

## 6. 트랜잭션 관리

### 6.1 기본 원칙

- Service 계층에서만 트랜잭션 관리
- 읽기 전용 트랜잭션 기본 설정
- 쓰기 작업 시 명시적으로 @Transactional 추가

```java

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용
public class UserService {

	// 조회는 readOnly = true (기본값)
	public UserResponse findById(Long id) {
		// ...
	}

	// 쓰기 작업은 명시적으로 표시
	@Transactional
	public UserResponse createUser(UserRequest request) {
		// ...
	}

	@Transactional
	public void updateUser(Long id, UserRequest request) {
		// ...
	}
}
```

## 7. 코드 최소화 원칙

### 7.1 Controller 최소화

- HTTP 바인딩과 응답만 처리
- 로직은 모두 Service로 위임

### 7.2 Entity 최소화

- 데이터 정의와 JPA 매핑만 포함
- 복잡한 비즈니스 로직 금지
- 단순한 도메인 로직만 허용 (예: isActive())

```java
// Good - 단순한 Entity
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String email;
	private String name;
	private boolean active;

	// 단순한 도메인 로직만 허용
	public boolean isActive() {
		return this.active;
	}
}
```

