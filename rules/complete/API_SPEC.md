# API 엔드포인트 명세 (Phase 4 완료 기준)

**작성일**: 2025-01-27  
**버전**: v1.0.0

---

## 🔐 인증 API

### 1. 회원가입

#### 기본 정보

- **URL**: `/api/auth/signup`
- **Method**: `POST`
- **인증**: 불필요
- **Content-Type**: `application/json`

#### 요청 본문

```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

| 필드       | 타입     | 필수 | 제약 조건         | 설명        |
|----------|--------|----|---------------|-----------|
| email    | String | O  | 이메일 형식, 중복 불가 | 사용자 이메일   |
| password | String | O  | 8자 이상         | 비밀번호 (평문) |
| name     | String | O  | 1자 이상         | 사용자 이름    |

#### 성공 응답 (201 Created)

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "createdAt": "2025-01-27T10:30:00"
  },
  "error": null
}
```

#### 실패 응답

**중복 이메일 (409 Conflict)**

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

**유효성 검증 실패 (400 Bad Request)**

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "COMMON001",
    "message": "입력값이 올바르지 않습니다"
  }
}
```

---

### 2. 로그인 ⭐ NEW

#### 기본 정보

- **URL**: `/api/auth/login`
- **Method**: `POST`
- **인증**: 불필요
- **Content-Type**: `application/json`

#### 요청 본문

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

| 필드       | 타입     | 필수 | 제약 조건  | 설명        |
|----------|--------|----|--------|-----------|
| email    | String | O  | 이메일 형식 | 사용자 이메일   |
| password | String | O  | 1자 이상  | 비밀번호 (평문) |

#### 성공 응답 (200 OK)

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTcwNjMyNDQwMCwiZXhwIjoxNzA2MzI4MDAwfQ.xxxxx",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA2MzI0NDAwLCJleHAiOjE3MDY5MjkyMDB9.yyyyy"
  },
  "error": null
}
```

| 필드           | 타입     | 설명                           |
|--------------|--------|------------------------------|
| accessToken  | String | JWT Access Token (유효기간: 1시간) |
| refreshToken | String | JWT Refresh Token (유효기간: 7일) |

#### JWT Access Token 구조

```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "user@example.com",
  "authorities": ["ROLE_USER"],
  "iat": 1706324400,
  "exp": 1706328000
}
```

#### JWT Refresh Token 구조

```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "user@example.com",
  "iat": 1706324400,
  "exp": 1706929200
}
```

#### 실패 응답

**인증 실패 (401 Unauthorized)**

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

> ⚠️ **보안 주의사항**: 이메일이 존재하지 않는 경우와 비밀번호가 틀린 경우를 구분하지 않고 동일한 메시지를 반환합니다. (계정 존재 여부 노출 방지)

**유효성 검증 실패 (400 Bad Request)**

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "COMMON001",
    "message": "입력값이 올바르지 않습니다"
  }
}
```

---

## 🔄 토큰 사용 방법

### Access Token 사용

```http
GET /api/protected-resource
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Refresh Token 사용 (Phase 5 이후 구현 예정)

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 🧪 테스트 예시

### cURL

```bash
# 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "홍길동"
  }'

# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### HTTPie

```bash
# 회원가입
http POST http://localhost:8080/api/auth/signup \
  email=test@example.com \
  password=password123 \
  name=홍길동

# 로그인
http POST http://localhost:8080/api/auth/login \
  email=test@example.com \
  password=password123
```

### Postman Collection

```json
{
  "info": {
    "name": "OneDay API - Auth",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "회원가입",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"test@example.com\",\n  \"password\": \"password123\",\n  \"name\": \"홍길동\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/auth/signup",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "8080",
          "path": [
            "api",
            "auth",
            "signup"
          ]
        }
      }
    },
    {
      "name": "로그인",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"test@example.com\",\n  \"password\": \"password123\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/auth/login",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "8080",
          "path": [
            "api",
            "auth",
            "login"
          ]
        }
      }
    }
  ]
}
```

---

## 📊 에러 코드 전체 목록

| 코드          | HTTP 상태 | 설명                           |
|-------------|---------|------------------------------|
| COMMON001   | 400     | 입력값이 올바르지 않습니다               |
| COMMON002   | 401     | 인증이 필요합니다                    |
| COMMON003   | 403     | 접근 권한이 없습니다                  |
| COMMON004   | 404     | 요청한 리소스를 찾을 수 없습니다           |
| COMMON999   | 500     | 서버 내부 오류가 발생했습니다             |
| AUTH001     | 409     | 이미 사용 중인 이메일입니다              |
| **AUTH002** | **401** | **이메일 또는 비밀번호가 올바르지 않습니다** ⭐ |
| AUTH003     | 401     | 유효하지 않은 토큰입니다                |
| AUTH004     | 401     | 만료된 토큰입니다                    |
| AUTH005     | 404     | 사용자를 찾을 수 없습니다               |

---

## 🚀 다음 구현 예정 API (Phase 5)

- `GET /api/auth/me` - 내 정보 조회 (인증 필요)
- `POST /api/auth/refresh` - Access Token 갱신 (Refresh Token 필요)
- `POST /api/auth/logout` - 로그아웃 (인증 필요)

---

**Updated**: 2025-01-27

