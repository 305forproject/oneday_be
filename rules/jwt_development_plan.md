---
apply: JWT 인증/인가 기능 개발 시
---

# JWT 인증/인가 TDD 개발 계획

## 개발 원칙

- **TDD (Test-Driven Development)**: 테스트 먼저 작성 후 구현 (Red-Green-Refactor)
- **YAGNI**: 요구사항에 명시된 기능만 구현
- **코드 규칙 준수**: architecture.md, code_style.md, api_design.md 등 모든 규칙 준수
- **테스트 커버리지**: Service 계층 80% 목표
- **테스트 패턴**: Given-When-Then 패턴 사용

---

## Phase 1: 기반 구조 설정

### 1단계: 의존성 및 설정

**파일**: `build.gradle`, `application.yml`

**작업 내용**:

- `build.gradle` 의존성 추가:
    - Spring Security
    - JWT 라이브러리 (jjwt-api, jjwt-impl, jjwt-jackson)
- `application.yml` JWT 설정:
    - `jwt.secret`: JWT 서명용 시크릿 키
    - `jwt.access-token-expiration`: Access Token 만료 시간 (예: 3600000ms = 1시간)
    - `jwt.refresh-token-expiration`: Refresh Token 만료 시간 (예: 604800000ms = 7일)

**디렉토리 구조** (레이어 우선):

```
src/main/java/com/oneday/core/
├── controller/
│   ├── user/
│   │   └── UserController.java
│   └── auth/
│       └── AuthController.java
├── service/
│   ├── user/
│   │   ├── UserService.java
│   │   └── CustomUserDetailsService.java
│   └── auth/
│       └── AuthService.java
├── repository/
│   └── user/
│       └── UserRepository.java
├── entity/
│   └── User.java
├── dto/
│   ├── user/
│   ├── auth/
│   │   ├── SignUpRequest.java
│   │   ├── SignUpResponse.java
│   │   ├── LoginRequest.java
│   │   └── LoginResponse.java
│   └── common/
│       └── ApiResponse.java
├── exception/
│   ├── auth/
│   │   ├── DuplicateEmailException.java
│   │   ├── InvalidCredentialsException.java
│   │   ├── InvalidTokenException.java
│   │   └── ExpiredTokenException.java
│   ├── CustomException.java
│   ├── ErrorCode.java
│   └── GlobalExceptionHandler.java
├── config/
│   └── security/
│       ├── SecurityConfig.java
│       ├── JwtProperties.java
│       ├── JwtTokenProvider.java
│       └── JwtAuthenticationFilter.java
└── util/
```

---

### 2단계: 엔티티 설계

**파일**:

- `src/main/java/com/oneday/core/entity/User.java`
- `src/main/java/com/oneday/core/entity/Role.java` (enum)
- `src/main/java/com/oneday/core/repository/user/UserRepository.java`

**User 엔티티 요구사항**:

- `UserDetails` 인터페이스 구현
- 필드:
    - `id` (Long, PK)
    - `email` (String, unique)
    - `password` (String, 암호화됨)
    - `name` (String)
    - `role` (Role enum)
    - `createdAt` (LocalDateTime)
    - `updatedAt` (LocalDateTime)
- Lombok: `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`

**Role enum**:

```java
public enum Role {
    USER, ADMIN
}
```

**UserRepository**:

- JpaRepository 상속
- `Optional<User> findByEmail(String email)`
- `boolean existsByEmail(String email)`

---

## Phase 2: JWT 핵심 기능 (TDD)

### 3단계: JWT 유틸리티 클래스

**파일**:

- `src/test/java/com/oneday/core/config/security/JwtTokenProviderTest.java` (테스트)
- `src/main/java/com/oneday/core/config/security/JwtTokenProvider.java` (구현)

**테스트 케이스** (`JwtTokenProviderTest`):

1. `토큰_생성_성공()`: username으로 토큰 생성 검증
2. `유효한_토큰_검증_성공()`: 생성된 토큰이 유효함을 검증
3. `토큰에서_사용자명_추출_성공()`: 토큰에서 username 추출 검증
4. `만료된_토큰_검증_실패()`: 만료된 토큰은 false 반환
5. `잘못된_토큰_검증_실패()`: 잘못된 형식의 토큰은 false 반환

**구현 메서드** (`JwtTokenProvider`):

- `String generateAccessToken(String username)`: Access Token 생성
- `String generateRefreshToken(String username)`: Refresh Token 생성
- `boolean validateToken(String token)`: 토큰 유효성 검증
- `String getUsernameFromToken(String token)`: 토큰에서 username 추출
- `Date getExpirationDateFromToken(String token)`: 토큰 만료일 추출

**기술 스택**:

- JJWT 라이브러리 사용
- HS256 알고리즘

---

### 4단계: 커스텀 UserDetailsService

**파일**:

- `src/test/java/com/oneday/core/service/user/CustomUserDetailsServiceTest.java` (테스트)
- `src/main/java/com/oneday/core/service/user/CustomUserDetailsService.java` (구현)

**테스트 케이스** (`CustomUserDetailsServiceTest`):

1. `이메일로_사용자_조회_성공()`: 존재하는 이메일로 UserDetails 반환
2. `존재하지_않는_사용자_예외_발생()`: 없는 이메일로 UsernameNotFoundException 발생

**구현** (`CustomUserDetailsService`):

- `UserDetailsService` 인터페이스 구현
- `loadUserByUsername(String email)`: 이메일로 사용자 조회
- UserRepository 의존성 주입

---

## Phase 3: 회원가입 기능 (TDD)

### 5단계: 회원가입 Service

**파일**:

- `src/test/java/com/oneday/core/service/auth/AuthServiceTest.java` (테스트)
- `src/main/java/com/oneday/core/service/auth/AuthService.java` (구현)
- `src/main/java/com/oneday/core/dto/auth/SignUpRequest.java`
- `src/main/java/com/oneday/core/dto/auth/SignUpResponse.java`

**DTO 설계**:

`SignUpRequest`:

- `email` (String, @Email, @NotBlank)
- `password` (String, @NotBlank, @Size(min=8))
- `name` (String, @NotBlank)

`SignUpResponse`:

- `id` (Long)
- `email` (String)
- `name` (String)
- `createdAt` (LocalDateTime)

**테스트 케이스** (`AuthServiceTest`):

1. `회원가입_성공()`: 정상적인 회원가입 처리
2. `중복_이메일_예외_발생()`: 이미 존재하는 이메일로 DuplicateEmailException 발생
3. `비밀번호_암호화_확인()`: 저장된 비밀번호가 암호화되었는지 검증

**구현** (`AuthService`):

- `SignUpResponse signUp(SignUpRequest request)`: 회원가입 처리
- BCryptPasswordEncoder로 비밀번호 암호화
- UserRepository로 저장

---

### 6단계: 회원가입 Controller

**파일**:

- `src/test/java/com/oneday/core/controller/auth/AuthControllerTest.java` (테스트)
- `src/main/java/com/oneday/core/controller/auth/AuthController.java` (구현)

**테스트 케이스** (`AuthControllerTest`):

1. `회원가입_API_성공()`: POST /api/auth/signup 성공 (201 Created)
2. `유효성_검증_실패()`: 잘못된 요청 데이터로 400 Bad Request

**구현** (`AuthController`):

- `POST /api/auth/signup`: 회원가입 엔드포인트
- `@Valid` 어노테이션으로 유효성 검증
- ApiResponse 형식으로 응답

---

## Phase 4: 로그인 기능 (TDD)

### 7단계: 로그인 Service

**파일**:

- `src/test/java/com/oneday/core/service/auth/AuthServiceTest.java` (테스트 추가)
- `src/main/java/com/oneday/core/service/auth/AuthService.java` (구현 추가)
- `src/main/java/com/oneday/core/dto/auth/LoginRequest.java`
- `src/main/java/com/oneday/core/dto/auth/LoginResponse.java`

**DTO 설계**:

`LoginRequest`:

- `email` (String, @Email, @NotBlank)
- `password` (String, @NotBlank)

`LoginResponse`:

- `accessToken` (String)
- `refreshToken` (String)
- `tokenType` (String, "Bearer")
- `expiresIn` (Long, 초 단위)

**테스트 케이스** (`AuthServiceTest`):

1. `로그인_성공()`: 올바른 이메일/비밀번호로 토큰 발급
2. `잘못된_비밀번호_예외_발생()`: 틀린 비밀번호로 InvalidCredentialsException 발생
3. `존재하지_않는_사용자_예외_발생()`: 없는 이메일로 InvalidCredentialsException 발생
4. `JWT_토큰_생성_검증()`: 반환된 토큰이 유효한지 검증

**구현** (`AuthService`):

- `LoginResponse login(LoginRequest request)`: 로그인 처리
- AuthenticationManager로 인증
- JwtTokenProvider로 토큰 생성

---

### 8단계: 로그인 Controller

**파일**:

- `src/test/java/com/oneday/core/controller/auth/AuthControllerTest.java` (테스트 추가)
- `src/main/java/com/oneday/core/controller/auth/AuthController.java` (구현 추가)

**테스트 케이스** (`AuthControllerTest`):

1. `로그인_API_성공()`: POST /api/auth/login 성공 (200 OK)
2. `인증_실패()`: 잘못된 자격증명으로 401 Unauthorized

**구현** (`AuthController`):

- `POST /api/auth/login`: 로그인 엔드포인트
- `@Valid` 어노테이션으로 유효성 검증
- ApiResponse 형식으로 응답

---

## Phase 5: JWT 필터 및 인가 (TDD)

### 9단계: JWT 인증 필터

**파일**:

- `src/test/java/com/oneday/core/config/security/JwtAuthenticationFilterTest.java` (테스트)
- `src/main/java/com/oneday/core/config/security/JwtAuthenticationFilter.java` (구현)

**테스트 케이스** (`JwtAuthenticationFilterTest`):

1. `유효한_토큰으로_인증_성공()`: Authorization 헤더에 유효한 토큰이 있으면 인증 성공
2. `토큰_없을_때_필터_통과()`: 토큰이 없어도 필터는 통과 (인증 안됨 상태)
3. `만료된_토큰_인증_실패()`: 만료된 토큰은 인증 실패
4. `잘못된_토큰_인증_실패()`: 잘못된 형식의 토큰은 인증 실패

**구현** (`JwtAuthenticationFilter`):

- `OncePerRequestFilter` 상속
- `doFilterInternal()` 구현:
    1. Authorization 헤더에서 토큰 추출
    2. JwtTokenProvider로 토큰 검증
    3. 유효하면 SecurityContext에 인증 정보 설정
    4. 다음 필터로 전달

---

### 10단계: Security 설정

**파일**: `src/main/java/com/oneday/core/config/security/SecurityConfig.java`

**구현 내용**:

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

## Phase 6: 예외 처리 및 마무리

### 11단계: 커스텀 예외 및 에러 핸들링

**파일**:

- `src/main/java/com/oneday/core/exception/ErrorCode.java`
- `src/main/java/com/oneday/core/exception/auth/DuplicateEmailException.java`
- `src/main/java/com/oneday/core/exception/auth/InvalidCredentialsException.java`
- `src/main/java/com/oneday/core/exception/auth/InvalidTokenException.java`
- `src/main/java/com/oneday/core/exception/auth/ExpiredTokenException.java`
- `src/main/java/com/oneday/core/exception/GlobalExceptionHandler.java`

**ErrorCode enum 추가**:

```java
// 인증/인가 관련
DUPLICATE_EMAIL(409, "AUTH001", "이미 사용 중인 이메일입니다"),
INVALID_CREDENTIALS(401, "AUTH002", "이메일 또는 비밀번호가 올바르지 않습니다"),
INVALID_TOKEN(401, "AUTH003", "유효하지 않은 토큰입니다"),
EXPIRED_TOKEN(401, "AUTH004", "만료된 토큰입니다"),
UNAUTHORIZED(401, "AUTH005", "인증이 필요합니다"),
FORBIDDEN(403, "AUTH006", "접근 권한이 없습니다"),
```

**커스텀 예외 클래스**:

- `DuplicateEmailException`: 이메일 중복 시
- `InvalidCredentialsException`: 잘못된 로그인 정보
- `InvalidTokenException`: 유효하지 않은 토큰
- `ExpiredTokenException`: 만료된 토큰

**GlobalExceptionHandler 업데이트**:

- 인증/인가 예외 처리 메서드 추가
- AccessDeniedException, AuthenticationException 처리

---

### 12단계: 통합 테스트

**파일**: `src/test/java/com/oneday/core/integration/AuthIntegrationTest.java`

**테스트 시나리오**:

1. `회원가입_로그인_인증_성공_시나리오()`:
    - 회원가입 → 로그인 → 인증 필요 API 호출 성공

2. `토큰_만료_시나리오()`:
    - 만료된 토큰으로 API 호출 시 401 Unauthorized

3. `권한_부족_시나리오()`:
    - USER 권한으로 ADMIN 전용 API 호출 시 403 Forbidden

**설정**:

- `@SpringBootTest`
- `@AutoConfigureMockMvc`
- 실제 DB 또는 H2 인메모리 DB 사용

---

## Phase 7: 추가 기능 (요구사항 확인 후)

### 13단계: Refresh Token 기능 (선택)

- Refresh Token 저장소 설계 (Redis 또는 DB)
- Token 갱신 API: `POST /api/auth/refresh`
- TDD로 테스트 및 구현

### 14단계: 로그아웃 기능 (선택)

- Token Blacklist 구현 (Redis)
- 로그아웃 API: `POST /api/auth/logout`
- TDD로 테스트 및 구현

---

## 체크리스트

### Phase 1

- [x] build.gradle 의존성 추가
- [x] application.yml JWT 설정
- [x] JwtProperties 클래스 작성
- [x] 디렉토리 구조 생성
- [x] User 엔티티 작성
- [x] Role enum 작성
- [x] UserRepository 작성
- [x] UserService 작성
- [x] UserServiceTest 작성 (testing.md 규칙 준수)

### Phase 2

- [ ] JwtTokenProviderTest 작성
- [ ] JwtTokenProvider 구현
- [ ] CustomUserDetailsServiceTest 작성
- [ ] CustomUserDetailsService 구현

### Phase 3

- [ ] SignUpRequest/Response DTO 작성
- [ ] AuthServiceTest (회원가입) 작성
- [ ] AuthService (회원가입) 구현
- [ ] AuthControllerTest (회원가입) 작성
- [ ] AuthController (회원가입) 구현

### Phase 4

- [ ] LoginRequest/Response DTO 작성
- [ ] AuthServiceTest (로그인) 작성
- [ ] AuthService (로그인) 구현
- [ ] AuthControllerTest (로그인) 작성
- [ ] AuthController (로그인) 구현

### Phase 5

- [ ] JwtAuthenticationFilterTest 작성
- [ ] JwtAuthenticationFilter 구현
- [ ] SecurityConfig 작성

### Phase 6

- [ ] ErrorCode enum 업데이트
- [ ] 커스텀 예외 클래스 작성
- [ ] GlobalExceptionHandler 업데이트
- [ ] AuthIntegrationTest 작성

### Phase 7 (선택)

- [ ] Refresh Token 기능
- [ ] 로그아웃 기능

---

## 참고 규칙 문서

- `architecture.md`: 레이어 구조, MVC 패턴
- `code_style.md`: 네이밍 컨벤션, 포맷팅, YAGNI 원칙
- `api_design.md`: REST API 설계, 응답 형식
- `exception_handling.md`: 예외 처리 전략
- `database_jpa.md`: 엔티티 설계 규칙
- `testing.md`: 테스트 작성 규칙
- `logging.md`: 로그 작성 규칙

---

## 현재 진행 상황

- 현재 단계: **Phase 1 완료 ✅**
- 다음 단계: **Phase 2 - 3단계 (JWT 유틸리티 클래스)**

