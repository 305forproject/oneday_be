# Copilot Instructions

## 언어 설정
- 모든 코드 리뷰와 코멘트는 **한국어**로 작성해 주세요.
- Pull Request 리뷰 시 한국어로 피드백을 제공해 주세요.

## 리뷰 가이드라인
- 코드의 품질, 성능, 보안 관점에서 검토해 주세요.
- 개선 사항이 있다면 구체적인 제안을 한국어로 제공해 주세요.
- 버그나 잠재적 문제점을 발견하면 명확하게 한국어로 설명해 주세요.

## 톤 앤 매너
- 건설적이고 도움이 되는 톤으로 리뷰해 주세요.
- 비판보다는 개선을 위한 제안에 집중해 주세요.
- 긍정적인 피드백도 함께 제공해 주세요.

---

## 프로젝트 기술 스택
- **프레임워크**: Spring Boot 3.5.7
- **언어**: Java 21
- **빌드 도구**: Gradle
- **데이터베이스**: MySQL + Spring Data JPA
- **보안**: Spring Security + JWT (Access Token + Refresh Token)
- **문서화**: SpringDoc OpenAPI (Swagger)
- **테스트**: JUnit 5 + Mockito + Spring Security Test
- **유틸리티**: Lombok, Jakarta Validation

---

## 아키텍처 및 설계 원칙

### 계층형 아키텍처 (Layered Architecture)
프로젝트는 명확한 계층 분리를 따릅니다:
```
Controller → Service → Repository → Entity
```

**각 계층의 책임:**
- **Controller**: HTTP 요청/응답 처리, 입력 검증, 적절한 HTTP 상태 코드 반환
- **Service**: 비즈니스 로직 구현, 트랜잭션 관리
- **Repository**: 데이터 접근 계층 (JPA)
- **Entity**: 도메인 모델 (JPA Entity)

**패키지 구조:**
```
com.oneday.core
├── config/         # 설정 클래스 (Security, JWT 등)
├── controller/     # REST API 컨트롤러
├── service/        # 비즈니스 로직
├── repository/     # 데이터 접근 계층
├── entity/         # JPA 엔티티
├── dto/            # 데이터 전송 객체
├── exception/      # 예외 클래스
└── util/           # 유틸리티 클래스
```

**리뷰 체크포인트:**
- ✅ Controller는 비즈니스 로직을 포함하지 않는가?
- ✅ Service는 여러 Repository를 조합하여 비즈니스 로직을 처리하는가?
- ✅ Repository는 단순 데이터 접근만 담당하는가?
- ✅ 계층 간 의존성이 단방향인가? (하위 계층이 상위 계층을 의존하지 않는가?)

---

## 예외 처리 규칙

### 통합 예외 처리
모든 예외는 `@RestControllerAdvice`를 사용한 `GlobalExceptionHandler`에서 중앙 집중식으로 처리합니다.

**예외 처리 구조:**
```java
// 1. ErrorCode Enum으로 에러 정의
public enum ErrorCode {
    INVALID_INPUT(400, "COMMON001", "입력값이 올바르지 않습니다"),
    DUPLICATE_EMAIL(409, "AUTH001", "이미 사용 중인 이메일입니다"),
    // ...
}

// 2. CustomException 상속
public class DuplicateEmailException extends CustomException {
    public DuplicateEmailException(String message) {
        super(ErrorCode.DUPLICATE_EMAIL, message);
    }
}

// 3. GlobalExceptionHandler에서 처리
@ExceptionHandler(CustomException.class)
public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
    // 통합 처리
}
```

**리뷰 체크포인트:**
- ✅ 새로운 예외가 `CustomException`을 상속받는가?
- ✅ 예외 이름이 `{Domain}{Reason}Exception` 형식인가? (예: `DuplicateEmailException`)
- ✅ 도메인별로 예외가 패키지로 분리되어 있는가? (예: `exception.auth`)
- ✅ `ErrorCode`에 적절한 HTTP 상태 코드와 에러 코드가 정의되어 있는가?
- ✅ 예외 발생 시 적절한 로그가 남는가? (`log.warn` 또는 `log.error`)
- ✅ 예외 메시지가 사용자에게 유의미한가?

---

## API 설계 규칙

### RESTful API 설계
**URL 패턴:**
```
/api/{도메인}/{리소스}/{액션}
```

**예시:**
```
POST   /api/auth/signup      # 회원가입
POST   /api/auth/login       # 로그인
POST   /api/auth/refresh     # 토큰 갱신
GET    /api/auth/me          # 인증된 사용자 정보
```

### 공통 응답 형식 (ApiResponse)
모든 API는 `ApiResponse<T>` 래퍼를 사용합니다:

```java
{
  "success": true,
  "data": { /* 실제 데이터 */ },
  "error": null
}

// 실패 시
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH001",
    "message": "이미 사용 중인 이메일입니다"
  }
}
```

**리뷰 체크포인트:**
- ✅ 모든 API가 `ApiResponse<T>`를 반환하는가?
- ✅ HTTP 메서드가 적절한가? (POST: 생성, GET: 조회, PUT/PATCH: 수정, DELETE: 삭제)
- ✅ HTTP 상태 코드가 적절한가? (200: OK, 201: Created, 400: Bad Request, 401: Unauthorized, 403: Forbidden, 404: Not Found, 409: Conflict, 500: Internal Server Error)
- ✅ URL이 RESTful 규칙을 따르는가?
- ✅ `@Valid`를 사용하여 입력 검증을 수행하는가?

---

## DTO (Data Transfer Object) 규칙

### Java Record 사용
DTO는 Java 17의 Record를 사용하여 불변 객체로 구현합니다:

```java
public record LoginRequest(
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다")
    String password
) {}
```

**네이밍 규칙:**
- Request DTO: `{Action}Request` (예: `LoginRequest`, `SignUpRequest`)
- Response DTO: `{Action}Response` (예: `LoginResponse`, `SignUpResponse`)

**리뷰 체크포인트:**
- ✅ DTO가 Java Record로 구현되어 있는가?
- ✅ Jakarta Validation 어노테이션이 적절히 사용되었는가? (`@NotBlank`, `@Email`, `@Size` 등)
- ✅ Validation 메시지가 한국어로 작성되어 있는가?
- ✅ DTO 네이밍이 `{Action}Request/Response` 형식을 따르는가?
- ✅ DTO가 도메인별로 패키지에 분리되어 있는가? (예: `dto.auth`, `dto.common`)
- ✅ DTO에 비즈니스 로직이 포함되어 있지 않는가?

---

## Entity 및 JPA 규칙

### Entity 설계
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"password"})  // 민감 정보 제외
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Builder
    public User(String email, String password, String name, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role != null ? role : Role.USER;
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

**리뷰 체크포인트:**
- ✅ `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 사용으로 불변성 보호하는가?
- ✅ `@Builder` 패턴을 사용하는가?
- ✅ 민감 정보가 `@ToString`에서 제외되어 있는가?
- ✅ `@Column`에 적절한 제약조건이 설정되어 있는가? (`nullable`, `unique`, `length`)
- ✅ 테이블명이 복수형인가? (예: `users`, `refresh_tokens`)
- ✅ Enum이 `@Enumerated(EnumType.STRING)`으로 저장되는가? (ORDINAL 사용 금지)
- ✅ ID 생성 전략이 `GenerationType.IDENTITY`인가?
- ✅ Setter가 없고, 필요한 경우 비즈니스 메서드로 상태를 변경하는가?

### Repository 규칙
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

**리뷰 체크포인트:**
- ✅ 메서드 이름이 Spring Data JPA 규칙을 따르는가?
- ✅ 복잡한 쿼리는 `@Query` 어노테이션을 사용하는가?
- ✅ 반환 타입이 적절한가? (단일: `Optional<T>`, 다중: `List<T>`, 존재 확인: `boolean`)

---

## Service 계층 규칙

### 트랜잭션 관리
```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본은 읽기 전용
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional  // 쓰기 작업에만 트랜잭션
    public SignUpResponse signUp(SignUpRequest request) {
        // 비즈니스 로직
    }
    
    public LoginResponse login(LoginRequest request) {
        // 읽기 전용 로직
    }
}
```

**리뷰 체크포인트:**
- ✅ 클래스 레벨에 `@Transactional(readOnly = true)` 선언되어 있는가?
- ✅ 쓰기 작업 메서드에만 `@Transactional` 추가되어 있는가?
- ✅ 생성자 주입(`@RequiredArgsConstructor` + `final`)을 사용하는가?
- ✅ `@Slf4j`를 사용한 로깅이 적절히 추가되어 있는가?
- ✅ 비즈니스 로직이 Service에만 있고 Controller에는 없는가?
- ✅ 예외 처리가 명확한가?

---

## 보안 (Spring Security + JWT) 규칙

### 인증/인가 처리
**리뷰 체크포인트:**
- ✅ 비밀번호가 `PasswordEncoder`로 암호화되는가?
- ✅ JWT 토큰 생성 시 적절한 만료 시간이 설정되어 있는가?
- ✅ Refresh Token이 데이터베이스에 안전하게 저장되는가?
- ✅ `@AuthenticationPrincipal`을 사용하여 인증된 사용자 정보를 가져오는가?
- ✅ 보안에 민감한 정보가 로그에 남지 않는가?
- ✅ CORS 설정이 적절한가?

---

## 테스트 규칙

### 테스트 작성 원칙
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("인증 서비스 테스트")
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    @DisplayName("이메일로 사용자 조회 - 성공")
    void loadUserByUsername_Success() {
        // given
        String email = "user@example.com";
        User user = User.builder()
            .email(email)
            .password("encodedPassword")
            .name("테스트")
            .role(Role.USER)
            .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        
        // when
        UserDetails result = customUserDetailsService.loadUserByUsername(email);
        
        // then
        assertThat(result.getUsername()).isEqualTo(email);
        verify(userRepository, times(1)).findByEmail(email);
    }
}
```

**리뷰 체크포인트:**
- ✅ `@DisplayName`이 한국어로 작성되어 있는가?
- ✅ Given-When-Then 패턴을 따르는가?
- ✅ Mockito를 사용하여 의존성을 격리하는가?
- ✅ AssertJ를 사용한 단언문이 명확한가?
- ✅ 테스트 메서드 이름이 `{메서드명}_{시나리오}` 형식인가?
- ✅ 성공 케이스와 실패 케이스가 모두 테스트되는가?
- ✅ 테스트가 독립적으로 실행 가능한가? (다른 테스트에 의존하지 않는가?)

---

## 로깅 규칙

### 로깅 레벨 및 전략
```java
@Slf4j
public class AuthService {
    
    public SignUpResponse signUp(SignUpRequest request) {
        log.info("회원가입 시도: email={}", request.email());  // 주요 비즈니스 흐름
        
        if (userRepository.existsByEmail(request.email())) {
            log.warn("중복된 이메일로 회원가입 시도: {}", request.email());  // 경고
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다");
        }
        
        log.info("회원가입 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());
    }
    
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        log.error("CustomException: code={}, message={}", 
                  e.getErrorCode().getCode(), e.getMessage());  // 에러
    }
}
```

**로깅 레벨:**
- `log.info`: 주요 비즈니스 흐름 (API 호출, 중요 작업 완료)
- `log.warn`: 예상된 예외 상황 (중복 데이터, 유효하지 않은 요청)
- `log.error`: 예상하지 못한 에러 (시스템 오류, 예외)
- `log.debug`: 개발/디버깅용 상세 정보

**리뷰 체크포인트:**
- ✅ 모든 Service 클래스에 `@Slf4j`가 선언되어 있는가?
- ✅ API 진입점에 로그가 남는가?
- ✅ 로그 메시지가 한국어로 명확하게 작성되어 있는가?
- ✅ 민감한 정보(비밀번호, 토큰 등)가 로그에 남지 않는가?
- ✅ 로그 레벨이 적절한가?

---

## 코드 스타일 및 문서화

### Javadoc 주석
모든 public 클래스와 메서드에는 Javadoc 주석을 작성합니다:

```java
/**
 * 인증 관련 비즈니스 로직 처리
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Service
public class AuthService {
    
    /**
     * 회원가입
     * @param request 회원가입 요청 정보
     * @return 생성된 사용자 정보
     * @throws DuplicateEmailException 이메일이 이미 존재하는 경우
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        // 구현
    }
}
```

### 네이밍 규칙
- **클래스**: PascalCase (예: `AuthService`, `UserRepository`)
- **메서드/변수**: camelCase (예: `signUp`, `userRepository`)
- **상수**: UPPER_SNAKE_CASE (예: `MAX_LOGIN_ATTEMPTS`)
- **패키지**: 소문자 (예: `com.oneday.core.service.auth`)

### Lombok 사용
- `@Slf4j`: 로깅
- `@RequiredArgsConstructor`: 생성자 주입
- `@Getter`: Getter 메서드
- `@Builder`: 빌더 패턴
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`: 기본 생성자
- `@ToString(exclude = {...})`: toString (민감 정보 제외)

**리뷰 체크포인트:**
- ✅ 모든 클래스와 주요 메서드에 Javadoc 주석이 있는가?
- ✅ `@author`와 `@since` 태그가 포함되어 있는가?
- ✅ 네이밍 규칙을 따르는가?
- ✅ Lombok 어노테이션이 적절히 사용되었는가?
- ✅ 코드 내 주석이 한국어로 작성되어 있는가?
- ✅ 매직 넘버 대신 상수를 사용하는가?

---

## Git 커밋 및 PR 규칙

### 커밋 메시지
```
feat: 로그인 API 구현

- JWT 기반 인증 처리
- Refresh Token 저장 로직 추가
- 예외 처리 추가
```

**타입:**
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `refactor`: 리팩토링
- `test`: 테스트 추가/수정
- `docs`: 문서 수정
- `chore`: 빌드, 설정 변경

**리뷰 체크포인트:**
- ✅ 커밋 메시지가 명확한가?
- ✅ 한 커밋이 하나의 논리적 변경사항을 담고 있는가?
- ✅ 불필요한 파일이 커밋에 포함되지 않았는가?

---

## 성능 및 최적화

**리뷰 체크포인트:**
- ✅ N+1 쿼리 문제가 없는가? (필요시 `@EntityGraph` 사용)
- ✅ 불필요한 데이터베이스 조회가 없는가?
- ✅ 적절한 인덱스가 설정되어 있는가? (`@Column(unique = true)` 등)
- ✅ `@Transactional(readOnly = true)`를 적절히 사용하는가?
- ✅ 대용량 데이터 처리 시 페이징이 적용되어 있는가?

---

## 보안 체크리스트

**리뷰 체크포인트:**
- ✅ SQL Injection 방지: JPA 사용, `@Query`에서 파라미터 바인딩 사용
- ✅ XSS 방지: 입력값 검증 및 출력 시 이스케이프
- ✅ CSRF 방지: Spring Security 설정 확인
- ✅ 민감한 정보 노출 방지: 로그, 응답에 비밀번호/토큰 포함되지 않도록
- ✅ 적절한 권한 검증: `@PreAuthorize` 또는 `SecurityContext` 활용
- ✅ 입력값 검증: Jakarta Validation 적극 활용

---

## 리뷰 우선순위

### 🔴 Critical (반드시 수정)
- 보안 취약점
- 데이터 무결성 문제
- 심각한 성능 이슈
- 빌드/배포 차단 문제

### 🟡 Major (권장 수정)
- 아키텍처 원칙 위반
- 테스트 누락
- 예외 처리 미흡
- 코드 중복

### 🟢 Minor (개선 제안)
- 네이밍 개선
- 주석 추가
- 리팩토링 제안
- 성능 최적화 제안

---

## 리뷰 시 칭찬할 포인트
- ✅ 명확한 책임 분리
- ✅ 포괄적인 테스트 커버리지
- ✅ 잘 작성된 문서/주석
- ✅ 적절한 예외 처리
- ✅ 보안 고려
- ✅ 성능 최적화
- ✅ 일관된 코드 스타일
