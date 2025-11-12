# JWT 인증/인가 개발 가이드 📘

> 💡 **이 문서는 처음 JWT를 구현하는 개발자를 위한 단계별 가이드입니다.**

---

## 📚 시작하기 전에

### JWT란 무엇인가요?

**JWT (JSON Web Token)**: 사용자 인증 정보를 안전하게 담은 토큰입니다.

```
로그인 → 서버가 JWT 토큰 발급 → 이후 요청마다 토큰으로 인증
```

### 전체 개발 흐름

```
Phase 1: 기반 구조 준비 (라이브러리, Entity)
    ↓
Phase 2: JWT 토큰 기능 (생성/검증)
    ↓
Phase 3: 회원가입 기능
    ↓
Phase 4: 로그인 기능
    ↓
Phase 5: JWT 필터와 보안 설정
    ↓
Phase 6: Refresh Token 갱신 API
    ↓
Phase 7: 로그아웃 API
    ↓
Phase 8: 에러 처리 및 마무리
```

---

## 🎯 학습 목표

이 가이드를 완료하면 다음을 할 수 있습니다:

✅ JWT 토큰의 원리를 이해하고 구현할 수 있습니다  
✅ Spring Security를 설정하고 활용할 수 있습니다  
✅ TDD 방식으로 안전한 코드를 작성할 수 있습니다  
✅ 인증/인가 기능을 처음부터 끝까지 만들 수 있습니다

---

## 📖 Phase별 가이드

### 🏗️ [Phase 1: 기반 구조 준비하기](plans/phase1_setup.md)

**예상 시간**: 1-2시간

**배울 내용**:

- JWT 라이브러리 설치
- 폴더 구조 이해
- User 엔티티 만들기
- UserRepository 만들기

**체크포인트**:

- [ ] 애플리케이션이 정상 실행되나요?
- [ ] User 테이블이 생성되었나요?

---

### 🔐 [Phase 2: JWT 토큰 만들고 검증하기](plans/phase2_jwt_core.md)

**예상 시간**: 2-3시간

**배울 내용**:

- TDD 방식 이해하기
- JWT 토큰 생성 기능
- JWT 토큰 검증 기능
- 사용자 정보 조회 서비스

**체크포인트**:

- [ ] JwtTokenProvider 테스트가 모두 통과하나요?
- [ ] CustomUserDetailsService 테스트가 통과하나요?

---

### 👤 [Phase 3: 회원가입 기능 만들기](plans/phase3_signup.md)

**예상 시간**: 2-3시간

**배울 내용**:

- DTO 설계하기
- Service 계층 구현
- Controller 만들기
- 비밀번호 암호화

**체크포인트**:

- [ ] Postman으로 회원가입이 되나요?
- [ ] 중복 이메일은 거부되나요?
- [ ] 비밀번호가 암호화되어 저장되나요?

---

### 🔑 [Phase 4: 로그인 기능 만들기](plans/phase4_login.md)

**예상 시간**: 2-3시간

**배울 내용**:

- 로그인 로직 구현
- JWT 토큰 발급
- 인증 실패 처리

**체크포인트**:

- [ ] Postman으로 로그인이 되나요?
- [ ] JWT 토큰을 받았나요?
- [ ] 잘못된 비밀번호는 거부되나요?

---

### 🛡️ [Phase 5: JWT 필터와 보안 설정](plans/phase5_security.md)

**예상 시간**: 2-3시간

**배울 내용**:

- JWT 인증 필터 만들기
- Spring Security 설정
- 토큰으로 API 보호하기

**체크포인트**:

- [ ] 토큰 없이 API 호출 시 401 에러가 나나요?
- [ ] 유효한 토큰으로 API 호출이 되나요?

---

### 🔄 [Phase 6: Refresh Token 갱신 API](plans/phase6_refresh_token.md)

**예상 시간**: 2-3시간

**배울 내용**:

- Refresh Token 저장소 구현
- Token Refresh API 만들기
- Refresh Token Rotation 적용
- 토큰 만료 관리

**체크포인트**:

- [ ] Refresh Token이 DB에 저장되나요?
- [ ] 만료된 Access Token을 갱신할 수 있나요?
- [ ] Refresh Token도 함께 갱신되나요?
- [ ] 만료된 Refresh Token은 거부되나요?

---

### 🚪 [Phase 7: 로그아웃 API](plans/phase7_logout.md)

**예상 시간**: 1-2시간

**배울 내용**:

- 로그아웃 처리 로직
- Refresh Token 무효화
- SecurityContext 정리
- (선택) Access Token 블랙리스트

**체크포인트**:

- [ ] 로그아웃 후 Refresh Token이 삭제되나요?
- [ ] 로그아웃 후 토큰 갱신이 불가능한가요?
- [ ] 인증 없이 로그아웃 시도 시 401 에러가 나나요?

---

### 🚨 [Phase 8: 에러 처리 및 마무리](plans/phase8_error_handling.md)

**예상 시간**: 2-3시간

**배울 내용**:

- 커스텀 예외 만들기
- GlobalExceptionHandler 구현
- 통합 테스트 작성
- Refresh Token 예외 처리
- 로그아웃 예외 처리

**체크포인트**:

- [ ] 모든 에러가 적절한 메시지와 함께 반환되나요?
- [ ] Refresh Token 예외가 적절히 처리되나요?
- [ ] 통합 테스트가 모두 통과하나요?

---

## 📊 전체 진행 상황

### Phase 1: 기반 구조 준비

- [ ] 라이브러리 설치
- [ ] 폴더 구조 생성
- [ ] Entity 작성

### Phase 2: JWT 토큰 기능

- [ ] JwtTokenProvider 구현
- [ ] CustomUserDetailsService 구현

### Phase 3: 회원가입

- [ ] DTO 작성
- [ ] AuthService (signUp) 구현
- [ ] AuthController (signup) 구현

### Phase 4: 로그인

- [ ] DTO 작성
- [ ] AuthService (login) 구현
- [ ] AuthController (login) 구현

### Phase 5: 보안 설정

- [ ] JwtAuthenticationFilter 구현
- [ ] SecurityConfig 작성

### Phase 6: Refresh Token 갱신

- [ ] RefreshToken Entity 작성
- [ ] RefreshTokenRepository 작성
- [ ] Token Refresh DTO 작성
- [ ] AuthService (refreshToken) 구현
- [ ] AuthController (refresh) 구현
- [ ] login 메서드에 Refresh Token 저장 추가

### Phase 7: 로그아웃

- [ ] LogoutResponse DTO 작성
- [ ] AuthService (logout) 구현
- [ ] AuthController (logout) 구현
- [ ] (선택) Access Token 블랙리스트 구현
- [ ] (선택) 만료 토큰 정리 스케줄러

### Phase 8: 에러 처리

- [ ] 기본 예외 클래스 작성 (InvalidToken, ExpiredToken, Unauthorized)
- [ ] Refresh Token 예외 클래스 작성 (InvalidRefreshToken)
- [ ] GlobalExceptionHandler 구현
- [ ] Spring Security 예외 처리 추가
- [ ] 통합 테스트 작성 (전체 시나리오)

---

## 💡 개발 원칙

### 1. TDD (Test-Driven Development)

```
Red → Green → Refactor
1. 실패하는 테스트 먼저 작성
2. 테스트를 통과하는 최소한의 코드 작성
3. 코드를 깔끔하게 정리
```

### 2. YAGNI (You Aren't Gonna Need It)

- 요구사항에 있는 기능만 구현
- 미래를 위한 과도한 설계 지양

### 3. 한 번에 하나씩

- 한 Phase씩 집중해서 완료
- 서두르지 말고 차근차근

---

## 🛠️ 필요한 도구

### 필수

- Java 17 이상
- Spring Boot 3.x
- Gradle
- IDE (IntelliJ IDEA 권장)
- Postman (API 테스트)

### 선택

- Git (버전 관리)
- Docker (데이터베이스)

---

## 📚 참고 문서

개발 중 막히면 이 문서들을 참고하세요:

- **architecture.md**: 코드 구조화 방법
- **code_style.md**: 코딩 스타일 가이드
- **api_design.md**: API 설계 원칙
- **exception_handling.md**: 예외 처리 전략
- **database_jpa.md**: JPA 사용법

---

## 🎉 완료 후 다음 단계

### Phase 8까지 완료하면 기본 JWT 인증 시스템이 완성됩니다!

**완성된 기능**:

- ✅ 회원가입
- ✅ 로그인 (JWT 토큰 발급)
- ✅ 인증이 필요한 API 보호
- ✅ Access Token 갱신
- ✅ 로그아웃

### 추가로 구현할 수 있는 기능

#### 1. 보안 강화

- [ ] 이메일 인증 (회원가입 후)
- [ ] 비밀번호 찾기/재설정
- [ ] 2FA (Two-Factor Authentication)
- [ ] Rate Limiting (무차별 대입 공격 방지)
- [ ] Access Token 블랙리스트 (Redis)

#### 2. 사용자 관리

- [ ] 사용자 프로필 조회/수정
- [ ] 비밀번호 변경
- [ ] 계정 삭제
- [ ] 다중 기기 로그인 관리

#### 3. 권한 관리

- [ ] 역할 기반 권한 (ROLE_ADMIN, ROLE_USER)
- [ ] 세밀한 권한 제어 (메서드 레벨)
- [ ] 권한 계층 구조

#### 4. 소셜 로그인

- [ ] Google OAuth2
- [ ] Kakao Login
- [ ] Naver Login
- [ ] GitHub Login

#### 5. 모니터링 & 로깅

- [ ] 로그인/로그아웃 이력 저장
- [ ] 접속 로그 관리
- [ ] 보안 이벤트 모니터링
- [ ] 성능 지표 수집

### 프로덕션 배포 체크리스트

#### 보안

- [ ] JWT Secret Key를 환경변수로 분리
- [ ] HTTPS 적용 (SSL/TLS 인증서)
- [ ] CORS 정책 설정
- [ ] Rate Limiting 적용
- [ ] SQL Injection 방지 확인
- [ ] XSS 방지 확인

#### 성능

- [ ] 데이터베이스 인덱스 최적화
- [ ] 커넥션 풀 설정
- [ ] 캐싱 전략 (Redis)
- [ ] 로그 레벨 최적화

#### 운영

- [ ] 로그 수집 시스템 (ELK)
- [ ] 모니터링 대시보드 (Grafana)
- [ ] 알림 시스템 (Slack, Email)
- [ ] 백업 전략 수립

#### 문서화

- [ ] API 문서 (Swagger/OpenAPI)
- [ ] 배포 가이드
- [ ] 트러블슈팅 가이드
- [ ] 운영 매뉴얼

---

## 🌟 학습 완료!

축하합니다! 🎊

이 가이드를 완료하셨다면:

- **JWT 인증/인가의 원리**를 이해하고 있습니다
- **Spring Security**를 활용할 수 있습니다
- **TDD 방식**으로 개발할 수 있습니다
- **RESTful API**를 설계하고 구현할 수 있습니다

이제 실무 프로젝트에 JWT 인증 시스템을 적용할 준비가 되었습니다! 💪

---

## 📞 질문이나 피드백

이 가이드를 개선할 아이디어가 있다면:

1. 프로젝트 이슈에 등록
2. 더 나은 방법 제안
3. 오타나 오류 신고

**Happy Coding! 🚀**

- **testing.md**: 테스트 작성 가이드
- **logging.md**: 로그 작성 방법

---

## 💪 학습 팁

### 막힐 때

1. 에러 메시지를 자세히 읽어보세요
2. 테스트를 먼저 확인하세요
3. 규칙 문서를 참고하세요
4. 한 단계씩 되돌아가세요

### 효과적인 학습

1. 코드를 그냥 복사하지 말고 이해하면서 작성
2. 각 단계마다 Postman으로 직접 테스트
3. 테스트 코드를 먼저 읽고 이해
4. 막히면 쉬었다가 다시 시도

### 시간 관리

- 하루에 1-2 Phase 진행 권장
- 총 3-5일 소요 예상
- 서두르지 말고 확실히 이해하면서 진행

---

## 🚀 시작하기

준비되셨나요? 그럼 시작해봅시다!

👉 **[Phase 1: 기반 구조 준비하기](plans/phase1_setup.md)** 로 이동하세요!

---

## 🎓 추가 학습 (선택)

Phase 6 완료 후 더 배우고 싶다면:

- Refresh Token 구현
- 로그아웃 기능
- 이메일 인증
- OAuth 2.0 연동
- Redis를 이용한 토큰 관리

---

## ❓ FAQ

### Q1. Spring Security가 처음인데 괜찮을까요?

A. 네! 이 가이드는 초보자를 위해 작성되었습니다. 각 단계마다 자세한 설명이 있습니다.

### Q2. 테스트를 꼭 작성해야 하나요?

A. 네! 테스트는 코드가 제대로 작동하는지 확인하는 가장 좋은 방법입니다. 나중에 수정할 때도 안전합니다.

### Q3. 얼마나 걸리나요?

A. 개인차가 있지만, 보통 3-5일 정도 소요됩니다. 서두르지 말고 차근차근 진행하세요.

### Q4. 실제 프로젝트에 바로 적용해도 되나요?

A. Phase 6까지 완료하고 통합 테스트를 통과했다면 가능합니다. 단, 프로덕션 환경에서는 추가 보안 설정이 필요할 수 있습니다.

---

**화이팅! 🎉 궁금한 점이 있으면 언제든 질문하세요!**

