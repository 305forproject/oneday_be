---
apply: always
---

# 테스트 작성 가이드

## 1. 테스트 기본 원칙

### 1.1 테스트 목표

- **테스트 커버리지**: 80% 목표
- **테스트 중점 영역**: Service 레이어 (모든 비즈니스 로직 위치)
- **난이도**: 초보 주니어 개발자에게 적합한 테스트 전략

### 1.2 테스트 도구

- **JUnit 5** 사용
- **Mockito** (Mock 사용)
- **AssertJ** (가독성 높은 단언문)

## 2. Service 계층 테스트

### 2.1 기본 테스트 구조

```java
/**
 * UserService 테스트
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@Test
	@DisplayName("사용자 ID로 조회 - 성공")
	void findById_Success() {
		// given
		Long userId = 1L;
		User user = User.builder()
			.email("user@example.com")
			.name("홍길동")
			.build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// when
		UserResponse result = userService.findById(userId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getEmail()).isEqualTo("user@example.com");
		assertThat(result.getName()).isEqualTo("홍길동");
		verify(userRepository, times(1)).findById(userId);
	}

	@Test
	@DisplayName("사용자 ID로 조회 - 실패 (사용자 없음)")
	void findById_UserNotFound() {
		// given
		Long userId = 999L;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.findById(userId))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage("사용자를 찾을 수 없습니다.");

		verify(userRepository, times(1)).findById(userId);
	}
}
```

### 2.2 테스트 네이밍 규칙

- **형식**: `메서드명_시나리오_예상결과`
- **@DisplayName** 사용 권장 (한글 설명)

```java
// Good
@Test
@DisplayName("사용자 생성 - 성공")
void createUser_Success() {
}

@Test
@DisplayName("사용자 생성 - 실패 (이메일 중복)")
void createUser_DuplicateEmail() {
}

// Bad
@Test
void test1() {
}

@Test
void userCreateTest() {
}
```

### 2.3 Given-When-Then 패턴

```java

@Test
@DisplayName("사용자 생성 - 성공")
void createUser_Success() {
	// given (준비)
	UserRequest request = new UserRequest("user@example.com", "홍길동");
	User user = User.builder()
		.email(request.getEmail())
		.name(request.getName())
		.build();

	when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
	when(userRepository.save(any(User.class))).thenReturn(user);

	// when (실행)
	UserResponse result = userService.createUser(request);

	// then (검증)
	assertThat(result).isNotNull();
	assertThat(result.getEmail()).isEqualTo("user@example.com");
	assertThat(result.getName()).isEqualTo("홍길동");

	verify(userRepository, times(1)).existsByEmail(request.getEmail());
	verify(userRepository, times(1)).save(any(User.class));
}
```

## 3. Mock 사용 가이드

### 3.1 Mock 기본 사용법

```java

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@Test
	void testExample() {
		// Mock 동작 정의
		when(userRepository.findById(1L))
			.thenReturn(Optional.of(user));

		// Mock 호출 검증
		verify(userRepository, times(1)).findById(1L);
	}
}
```

### 3.2 Mock 동작 정의

```java
// 특정 값 반환
when(userRepository.findById(1L)).

thenReturn(Optional.of(user));

// 예외 발생
when(userRepository.findById(999L))
	.

thenThrow(new UserNotFoundException());

// 어떤 인자든 허용
when(userRepository.save(any(User.class)))
	.

thenReturn(user);
```

### 3.3 Mock 검증

```java
// 메서드가 1번 호출되었는지 검증
verify(userRepository, times(1)).

findById(1L);

// 메서드가 호출되지 않았는지 검증
verify(userRepository, never()).

delete(any(User.class));

// 메서드가 최소 1번 호출되었는지 검증
verify(userRepository, atLeastOnce()).

save(any(User.class));
```

## 4. 테스트 데이터 재사용

### 4.1 @BeforeEach 활용

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User user;
	private UserRequest userRequest;

	// ✅ 공통 테스트 데이터는 @BeforeEach에서 초기화
	@BeforeEach
	void setUp() {
		user = User.builder()
			.email("user@example.com")
			.name("홍길동")
			.build();

		userRequest = new UserRequest("user@example.com", "홍길동");
	}
}
```

**주의사항**:

- 테스트 간 격리를 위해 매 테스트마다 새로운 객체 생성
- `@BeforeEach`는 각 테스트 실행 전에 호출됨
- 공통으로 사용되는 테스트 데이터만 `@BeforeEach`에 작성
- 특정 테스트에만 필요한 데이터는 해당 테스트 메서드 내에서 생성

## 5. 테스트 케이스 작성 예시

### 5.1 CRUD 테스트

```java

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User user;
	private UserRequest userRequest;

	@BeforeEach
	void setUp() {
		user = User.builder()
			.email("user@example.com")
			.name("홍길동")
			.build();

		userRequest = new UserRequest("user@example.com", "홍길동");
	}

	@Test
	@DisplayName("사용자 생성 - 성공")
	void createUser_Success() {
		// given
		when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
		when(userRepository.save(any(User.class))).thenReturn(user);

		// when
		UserResponse result = userService.createUser(userRequest);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getEmail()).isEqualTo(userRequest.getEmail());
		assertThat(result.getName()).isEqualTo(userRequest.getName());
	}

	@Test
	@DisplayName("사용자 생성 - 실패 (이메일 중복)")
	void createUser_DuplicateEmail() {
		// given
		when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> userService.createUser(userRequest))
			.isInstanceOf(DuplicateEmailException.class);

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("사용자 조회 - 성공")
	void findById_Success() {
		// given
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// when
		UserResponse result = userService.findById(userId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getEmail()).isEqualTo(user.getEmail());
	}

	@Test
	@DisplayName("사용자 삭제 - 성공")
	void deleteUser_Success() {
		// given
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// when
		userService.deleteUser(userId);

		// then
		verify(userRepository, times(1)).delete(user);
	}
}
```

## 5. AssertJ 단언문 사용

### 5.1 기본 단언문

```java
// 동등성 검사
assertThat(result).

isEqualTo(expected);

assertThat(result).

isNotEqualTo(expected);

// null 검사
assertThat(result).

isNull();

assertThat(result).

isNotNull();

// boolean 검사
assertThat(result).

isTrue();

assertThat(result).

isFalse();

// 문자열 검사
assertThat(result).

contains("substring");

assertThat(result).

startsWith("prefix");

assertThat(result).

endsWith("suffix");
```

### 5.2 컬렉션 단언문

```java
// 크기 검사
assertThat(list).

hasSize(3);

assertThat(list).

isEmpty();

assertThat(list).

isNotEmpty();

// 포함 검사
assertThat(list).

contains(element);

assertThat(list).

containsExactly(element1, element2);
```

### 5.3 예외 단언문

```java
// 예외 발생 검증
assertThatThrownBy(() ->userService.

findById(999L))
	.

isInstanceOf(UserNotFoundException .class)
    .

hasMessage("사용자를 찾을 수 없습니다.");

// 예외 발생하지 않음 검증
assertThatCode(() ->userService.

findById(1L))
	.

doesNotThrowAnyException();
```

## 6. 테스트 작성 Best Practices

### 6.1 하나의 테스트는 하나의 동작만 검증

```java
// Good
@Test
@DisplayName("사용자 생성 - 성공")
void createUser_Success() {
	// 생성 성공 케이스만 테스트
}

@Test
@DisplayName("사용자 생성 - 실패 (이메일 중복)")
void createUser_DuplicateEmail() {
	// 이메일 중복 실패 케이스만 테스트
}
```

### 6.2 테스트는 독립적으로 실행 가능해야 함

```java
// Good - 각 테스트가 독립적
@Test
void test1() {
	User user = createUser("user1@example.com", "사용자1");
	// 테스트
}

@Test
void test2() {
	User user = createUser("user2@example.com", "사용자2");
	// 테스트
}
```

### 6.3 의미 있는 테스트 데이터 사용

```java
// Good
User user = User.builder()
		.email("user@example.com")
		.name("홍길동")
		.build();

// Bad
User user = User.builder()
	.email("test")
	.name("test")
	.build();
```

## 7. 테스트 커버리지 확인

### 7.1 Gradle 설정

```gradle
plugins {
    id 'jacoco'
}

test {
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    dependsOn test
}
```

### 7.2 커버리지 확인 명령

```bash
./gradlew test jacocoTestReport
```

