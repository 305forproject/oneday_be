# Phase 6: Refresh Token 갱신 API - 완료

## 📅 작업 일자

2025-11-09

## ✅ 구현 완료 사항

### 1. **RefreshToken Entity 및 Repository**

- **파일**:
  - `src/main/java/com/oneday/core/entity/RefreshToken.java`
  - `src/main/java/com/oneday/core/repository/RefreshTokenRepository.java`
- **역할**: Refresh Token을 DB에 저장하고 관리
- **주요 기능**:
  - `@ManyToOne` 관계로 User Entity와 연결
  - `token` 컬럼에 unique 제약 설정
  - `isExpired()`: 토큰 만료 여부 확인
  - `update()`: 토큰 갱신 (Refresh Token Rotation)
  - Index 설정: `token`, `user_id`

### 2. **Token Refresh DTO**

- **파일**:
  - `src/main/java/com/oneday/core/dto/auth/TokenRefreshRequest.java`
  - `src/main/java/com/oneday/core/dto/auth/TokenRefreshResponse.java`
- **역할**: Refresh Token 갱신 요청/응답 데이터 정의
- **검증**: `@NotBlank`로 Refresh Token 필수 입력 검증

### 3. **예외 처리**

- **파일**: `src/main/java/com/oneday/core/exception/auth/InvalidRefreshTokenException.java`
- **역할**: 유효하지 않거나 만료된 Refresh Token 예외
- **GlobalExceptionHandler**: 401 Unauthorized 응답 (AUTH006)

### 4. **JwtTokenProvider 헬퍼 메서드 추가**

- **파일**: `src/main/java/com/oneday/core/config/security/JwtTokenProvider.java`
- **추가 메서드**:
  - `generateAccessToken(String email)`: 이메일로 Access Token 생성
  - `generateRefreshToken(String email)`: 이메일로 Refresh Token 생성
  - `getUserEmailFromToken(String token)`: JWT에서 이메일 추출
  - `getRefreshTokenExpirationTime()`: Refresh Token 만료 시간 (초)
  - `getAccessTokenExpirationTime()`: Access Token 만료 시간 (초)
  - `validateToken(String token)`: boolean 반환 (예외 없이 검증)

### 5. **AuthService - refreshToken() 구현**

- **파일**: `src/main/java/com/oneday/core/service/auth/AuthService.java`
- **주요 로직**:
  1. DB에서 Refresh Token 조회
  2. 만료 여부 확인 (만료 시 DB에서 삭제)
  3. JWT 서명 검증 (실패 시 DB에서 삭제)
  4. 사용자 이메일 추출
  5. 새로운 Access Token + Refresh Token 발급
  6. **Refresh Token Rotation**: DB의 Refresh Token 업데이트
- **보안**: 탈취된 토큰 재사용 방지

### 6. **AuthService - login() 수정**

- **파일**: `src/main/java/com/oneday/core/service/auth/AuthService.java`
- **변경사항**: 로그인 시 Refresh Token을 DB에 저장
- **메서드**: `saveOrUpdateRefreshToken(User user, String token)`
  - 기존 토큰이 있으면 업데이트
  - 없으면 새로 생성

### 7. **AuthController - refreshToken API**

- **파일**: `src/main/java/com/oneday/core/controller/auth/AuthController.java`
- **엔드포인트**: `POST /api/auth/refresh`
- **요청**: `TokenRefreshRequest { refreshToken }`
- **응답**: `TokenRefreshResponse { accessToken, refreshToken, tokenType, expiresIn }`

### 8. **SecurityConfig 경로 추가**

- **파일**: `src/main/java/com/oneday/core/config/security/SecurityConfig.java`
- **변경사항**: `/api/auth/refresh` 경로를 `permitAll()`에 추가

### 9. **테스트 작성**

- **AuthServiceTest**: 4개 테스트 추가
  1. ✅ 유효한 Refresh Token으로 갱신 성공
  2. ✅ 만료된 Refresh Token으로 요청 시 예외 발생
  3. ✅ 존재하지 않는 Refresh Token으로 요청 시 예외 발생
  4. ✅ JWT 검증 실패 시 예외 발생

- **AuthControllerTest**: 3개 테스트 추가
  1. ✅ 토큰 갱신 성공
  2. ✅ 토큰 갱신 실패 - 만료된 토큰 (401)
  3. ✅ 토큰 갱신 실패 - 빈 토큰 (400)

---

## 🎯 YAGNI 원칙 준수

### ✅ 구현한 것 (Phase 6 필요)

- Refresh Token 저장소 (DB)
- Token Refresh API
- Refresh Token Rotation (보안 강화)
- 만료/검증 예외 처리

### ❌ 구현하지 않은 것 (미래 Phase)

- **Phase 7**: 로그아웃 API
- 만료된 토큰 일괄 삭제 스케줄러
- 기기별 Refresh Token 관리 (멀티 디바이스)

---

## 🔄 Refresh Token 갱신 흐름

```
1. 로그인
   → Access Token (1시간) + Refresh Token (7일) 발급
   → Refresh Token을 DB에 저장

2. API 요청 (Access Token 사용)
   → JwtAuthenticationFilter 검증
   → 정상 처리

3. Access Token 만료 (1시간 후)
   → API 요청 → 401 Unauthorized

4. Refresh Token으로 갱신 요청
   POST /api/auth/refresh { refreshToken }
   ↓
   1) DB에서 Refresh Token 조회
   2) 만료 여부 확인
   3) JWT 서명 검증
   4) 새 Access Token + 새 Refresh Token 발급
   5) DB의 Refresh Token 업데이트 (Rotation)
   ↓
   → 새로운 토큰으로 계속 서비스 이용

5. Refresh Token도 만료 (7일 후)
   → 다시 로그인 필요
```

---

## 🛡️ Refresh Token Rotation 보안

### 왜 Rotation이 필요한가?

**문제**: Refresh Token이 탈취되면 공격자가 계속 새로운 토큰을 발급받을 수 있음

**해결**: 토큰을 갱신할 때마다 새로운 Refresh Token도 발급

**효과**:

- 한 번 사용한 Refresh Token은 즉시 무효화
- 탈취된 토큰으로 재요청 시 DB에 없어서 차단
- 정상 사용자가 먼저 갱신하면 공격자의 토큰은 자동 무효화

### 구현 방식

```java
// 1. DB에서 기존 토큰 조회
RefreshToken savedToken = refreshTokenRepository.findByToken(oldToken)
    .orElseThrow(...);

// 2. 새로운 토큰 발급
String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

// 3. DB 업데이트 (기존 토큰을 새 토큰으로 교체)
savedToken.

update(newRefreshToken, newExpiresAt);
refreshTokenRepository.

save(savedToken);

// 4. 기존 토큰은 이제 DB에 없으므로 재사용 불가
```

---

## 📊 테스트 결과

✅ **전체 테스트 34개 통과**

- AuthServiceTest: 11개 (회원가입 3 + 로그인 4 + Refresh 4)
- AuthControllerTest: 8개 (회원가입 2 + 로그인 2 + Refresh 3 + 기타 1)
- JwtTokenProviderTest: 9개
- JwtAuthenticationFilterTest: 6개

---

## 📝 API 명세

### POST /api/auth/refresh

**Request**:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200 OK)**:

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "error": null
}
```

**Error Response (401 Unauthorized)**:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH006",
    "message": "만료된 Refresh Token입니다"
  }
}
```

---

## 🧪 테스트 시나리오

### 정상 흐름

1. 로그인 → Access Token + Refresh Token 발급
2. Access Token으로 API 요청 (1시간 동안 사용)
3. Access Token 만료 → 401 에러
4. Refresh Token으로 갱신 요청 → 새 토큰 발급
5. 새 Access Token으로 계속 사용

### 보안 시나리오

1. **만료된 Refresh Token**

- DB에서 만료 확인 → 토큰 삭제 → 401 에러

2. **탈취된 Refresh Token**

- 정상 사용자가 먼저 갱신 → DB의 토큰 업데이트
- 공격자가 기존 토큰으로 요청 → DB에 없음 → 401 에러

3. **JWT 서명 위조**

- `validateToken()` 실패 → 토큰 삭제 → 401 에러

---

## 💡 주요 설계 결정

### 1. User Entity와 @ManyToOne 관계

**이유**:

- User를 삭제하면 연관된 RefreshToken도 자동 삭제 가능 (Cascade)
- 외래키 제약으로 데이터 무결성 보장
- 정수 비교 (user_id)로 조회 성능 향상

**확장성**:

- 멀티 디바이스 지원 시 한 사용자가 여러 토큰 보유 가능
- 현재는 `findByUser()`로 1개만 관리

### 2. Refresh Token Rotation

**이유**:

- OWASP 권장 보안 방식
- 탈취된 토큰 재사용 방지
- 탈취 감지 가능

### 3. 만료/검증 실패 시 DB에서 삭제

**이유**:

- 불필요한 데이터 정리
- 공격 시도 차단
- DB 공간 절약

### 4. @PrePersist로 createdAt 자동 설정

**이유**:

- Builder 패턴과 호환
- 엔티티 생성 시점 자동 기록

---

## 🔗 다음 단계

Phase 7에서는 **로그아웃 API**를 구현합니다:

- Refresh Token 무효화 (DB에서 삭제)
- SecurityContext 정리
- 로그아웃 후 재로그인 흐름

---

## 🎉 Phase 6 완료!

Refresh Token 갱신 API가 정상 작동하며, 보안이 강화된 토큰 관리 시스템이 구축되었습니다!

