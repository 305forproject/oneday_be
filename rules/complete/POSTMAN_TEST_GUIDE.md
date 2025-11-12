# Postman 테스트 가이드

## 목차

1. [개요](#개요)
2. [환경 설정](#환경-설정)
3. [Phase 3: 회원가입 API 테스트](#phase-3-회원가입-api-테스트)
4. [Phase 4: 로그인 API 테스트](#phase-4-로그인-api-테스트)
5. [Phase 5: JWT 인증 테스트](#phase-5-jwt-인증-테스트)
6. [Postman Collection 설정](#postman-collection-설정)

---

## 개요

### 테스트 전략

**단위 테스트 (JUnit)**

- ✅ Phase 3: 회원가입 API (완료)
- ✅ Phase 4: 로그인 API (완료)
- ⏳ Phase 5: `/me` API는 `@WebMvcTest` 환경에서 `@AuthenticationPrincipal` 작동 제한으로 통합 테스트 예정

**수동 테스트 (Postman)**

- 전체 API 엔드포인트 검증
- JWT 인증 흐름 테스트 (Phase 5)
- 실제 토큰 발급 및 인증 확인

**통합 테스트 (@SpringBootTest)**

- 🔜 Phase 6 이후 작성 예정
- 전체 Spring Context를 로드하여 실제 JWT 필터 체인 테스트

---

## 환경 설정

### Base URL 설정

```
http://localhost:8080
```

### 환경 변수 (Environment Variables)

1. Postman 좌측 상단 "Environments" 클릭
2. "+" 버튼으로 새 환경 생성: `OneDay Local`
3. 변수 추가:

- `base_url`: `http://localhost:8080`
- `access_token`: (빈 값, 로그인 후 자동 설정)
- `refresh_token`: (빈 값, 로그인 후 자동 설정)

---

## Phase 3: 회원가입 API 테스트

### 1. 회원가입 성공

**Request**

```
POST {{base_url}}/api/auth/signup
Content-Type: application/json
```

**Body (JSON)**

```json
{
  "email": "test@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

**Expected Response (201 Created)**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "test@example.com",
    "name": "홍길동",
    "createdAt": "2025-01-26T10:30:00"
  },
  "error": null
}
```

**Tests Script (자동 검증)**

```javascript
pm.test("Status code is 201", function () {
  pm.response.to.have.status(201);
});

pm.test("Response has success=true", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData.success).to.eql(true);
});

pm.test("User email is correct", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData.data.email).to.eql("test@example.com");
});
```

---

### 2. 회원가입 실패 - 중복 이메일

**Request**

```
POST {{base_url}}/api/auth/signup
Content-Type: application/json
```

**Body (JSON)**

```json
{
  "email": "test@example.com",
  "password": "password456",
  "name": "김철수"
}
```

**Expected Response (409 Conflict)**

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH001",
    "message": "이미 사용 중인 이메일입니다"
  }
}
```

**Tests Script**

```javascript
pm.test("Status code is 409", function () {
  pm.response.to.have.status(409);
});

pm.test("Error code is AUTH001", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData.error.code).to.eql("AUTH001");
});
```

---

### 3. 회원가입 실패 - 유효성 검증 오류

**Request**

```
POST {{base_url}}/api/auth/signup
Content-Type: application/json
```

**Body (JSON) - 잘못된 이메일 형식**

```json
{
  "email": "invalid-email",
  "password": "pass",
  "name": ""
}
```

**Expected Response (400 Bad Request)**

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "유효성 검증 실패",
    "details": {
      "email": "올바른 이메일 형식이 아닙니다",
      "password": "비밀번호는 최소 8자 이상이어야 합니다",
      "name": "이름은 필수입니다"
    }
  }
}
```

---

## Phase 4: 로그인 API 테스트

### 1. 로그인 성공

**Request**

```
POST {{base_url}}/api/auth/login
Content-Type: application/json
```

**Body (JSON)**

```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

**Expected Response (200 OK)**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "error": null
}
```

**Tests Script (토큰 자동 저장)**

```javascript
pm.test("Status code is 200", function () {
  pm.response.to.have.status(200);
});

pm.test("Response has tokens", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData.data.accessToken).to.be.a('string');
  pm.expect(jsonData.data.refreshToken).to.be.a('string');

  // 환경 변수에 토큰 저장
  pm.environment.set("access_token", jsonData.data.accessToken);
  pm.environment.set("refresh_token", jsonData.data.refreshToken);
});

pm.test("Access token is valid JWT", function () {
  var jsonData = pm.response.json();
  var token = jsonData.data.accessToken;
  pm.expect(token.split('.').length).to.eql(3);
});
```

---

### 2. 로그인 실패 - 잘못된 비밀번호

**Request**

```
POST {{base_url}}/api/auth/login
Content-Type: application/json
```

**Body (JSON)**

```json
{
  "email": "test@example.com",
  "password": "wrongpassword"
}
```

**Expected Response (401 Unauthorized)**

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH002",
    "message": "이메일 또는 비밀번호가 올바르지 않습니다"
  }
}
```

**Tests Script**

```javascript
pm.test("Status code is 401", function () {
  pm.response.to.have.status(401);
});

pm.test("Error code is AUTH002", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData.error.code).to.eql("AUTH002");
});
```

---

### 3. 로그인 실패 - 존재하지 않는 사용자

**Request**

```
POST {{base_url}}/api/auth/login
Content-Type: application/json
```

**Body (JSON)**

```json
{
  "email": "notexist@example.com",
  "password": "password123"
}
```

**Expected Response (401 Unauthorized)**

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH002",
    "message": "이메일 또는 비밀번호가 올바르지 않습니다"
  }
}
```

---

## Phase 5: JWT 인증 테스트

### 1. /me API - 인증 성공

**Request**

```
GET {{base_url}}/api/auth/me
Authorization: Bearer {{access_token}}
```

**Headers**

```
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

**Expected Response (200 OK)**

```json
{
  "success": true,
  "data": "Authenticated as: test@example.com",
  "error": null
}
```

**Tests Script**

```javascript
pm.test("Status code is 200", function () {
  pm.response.to.have.status(200);
});

pm.test("Response contains user email", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData.data).to.include("test@example.com");
});

pm.test("Success is true", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData.success).to.eql(true);
});
```

---

### 2. /me API - 인증 실패 (토큰 없음)

**Request**

```
GET {{base_url}}/api/auth/me
```

**Headers**

```
Content-Type: application/json
```

(Authorization 헤더 없음)

**Expected Response (401 Unauthorized)**

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH003",
    "message": "인증이 필요합니다"
  }
}
```

**Tests Script**

```javascript
pm.test("Status code is 401", function () {
  pm.response.to.have.status(401);
});

pm.test("Error indicates authentication required", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData.success).to.eql(false);
});
```

---

### 3. /me API - 인증 실패 (잘못된 토큰)

**Request**

```
GET {{base_url}}/api/auth/me
Authorization: Bearer invalid-token-string
```

**Expected Response (401 Unauthorized)**

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH004",
    "message": "유효하지 않은 토큰입니다"
  }
}
```

---

### 4. /me API - 인증 실패 (만료된 토큰)

**Request**

```
GET {{base_url}}/api/auth/me
Authorization: Bearer {{expired_access_token}}
```

**Expected Response (401 Unauthorized)**

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH005",
    "message": "토큰이 만료되었습니다"
  }
}
```

---

## Postman Collection 설정

### Collection 생성 및 구조

```
OneDay API
├── Environment: OneDay Local
├── Phase 3: 회원가입
│   ├── 1. 회원가입 성공
│   ├── 2. 회원가입 실패 - 중복 이메일
│   └── 3. 회원가입 실패 - 유효성 검증
├── Phase 4: 로그인
│   ├── 1. 로그인 성공
│   ├── 2. 로그인 실패 - 잘못된 비밀번호
│   └── 3. 로그인 실패 - 존재하지 않는 사용자
└── Phase 5: JWT 인증
    ├── 1. /me API - 인증 성공
    ├── 2. /me API - 인증 실패 (토큰 없음)
    ├── 3. /me API - 인증 실패 (잘못된 토큰)
    └── 4. /me API - 인증 실패 (만료된 토큰)
```

---

### Collection 레벨 Pre-request Script

Collection 설정 > Pre-request Scripts에 추가:

```javascript
// 공통 헤더 설정
pm.request.headers.add({
  key: 'Content-Type',
  value: 'application/json'
});

// 로그 출력
console.log('Request to: ' + pm.request.url);
console.log('Method: ' + pm.request.method);
```

---

### Authorization 설정 (Phase 5 폴더 레벨)

"Phase 5: JWT 인증" 폴더 설정:

1. Authorization 탭 선택
2. Type: `Bearer Token`
3. Token: `{{access_token}}`

이렇게 설정하면 폴더 내 모든 요청에 자동으로 Bearer 토큰이 추가됩니다.

---

## 테스트 실행 순서

### 1. 전체 시나리오 테스트

```
1. 회원가입 성공 (test@example.com)
2. 로그인 성공 → 토큰 발급 및 저장
3. /me API - 인증 성공 (저장된 토큰 사용)
4. 로그아웃 (Phase 6 구현 예정)
```

### 2. Collection Runner 사용

1. Collection 우클릭 > "Run collection"
2. 실행 순서:

- Phase 3: 회원가입 성공
- Phase 4: 로그인 성공
- Phase 5: /me API - 인증 성공

3. "Run" 버튼 클릭하여 자동 테스트 실행

---

## 주의사항

1. **테스트 순서**: 회원가입 → 로그인 → 인증 API 순서로 진행
2. **토큰 자동 저장**: 로그인 Tests 스크립트에서 토큰을 환경 변수에 저장
3. **중복 이메일**: 같은 이메일로 재테스트 시 DB에서 삭제 필요
4. **토큰 만료**: Access Token은 1시간 후 만료됨
5. **환경 선택**: 우측 상단에서 "OneDay Local" 환경 선택 필수

---

## 문제 해결

### 401 Unauthorized 오류

- 환경 변수에 `access_token`이 설정되어 있는지 확인
- 로그인 API를 먼저 호출하여 토큰 발급
- 토큰이 만료되었다면 다시 로그인

### 409 Conflict (중복 이메일)

- 다른 이메일 주소 사용
- 또는 DB에서 해당 사용자 삭제 후 재시도

### 500 Internal Server Error

- 서버 로그 확인 (IntelliJ 콘솔)
- DB 연결 상태 확인 (Docker MySQL 컨테이너)
- application.yml 설정 확인

---

## Export/Import

### Collection Export

1. Collection 우클릭 > "Export"
2. Format: Collection v2.1
3. 파일 저장: `OneDay-API.postman_collection.json`

### Environment Export

1. Environment 우클릭 > "Export"
2. 파일 저장: `OneDay-Local.postman_environment.json`

### Import

1. Postman 좌측 상단 "Import" 버튼
2. 파일 선택 또는 드래그 앤 드롭
3. Collection과 Environment 모두 Import

---

## 참고 자료

- [API 명세서](API_SPEC.md)
- [Phase 5 완료 문서](phase5-complete.md)
- [JWT Development Plan](../jwt_development_plan.md)

