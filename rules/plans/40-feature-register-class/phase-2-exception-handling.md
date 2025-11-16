# Phase 2: 예외 처리 구조

## 🎯 목표

클래스 등록 도메인의 예외 처리 구조를 구축합니다.

**예상 소요 시간**: 1시간

---

## ✅ 작업 체크리스트

### 1. ErrorCode Enum 확장

- [ ] `ErrorCode.java` 파일 열기
- [ ] CL001~CL008 에러 코드 추가
- [ ] HTTP 상태 코드 매핑 확인

### 2. exception.classes 패키지 생성

- [ ] `src/main/java/com/oneday/core/exception/classes` 디렉토리 생성

### 3. 도메인 예외 클래스 4개 생성

- [ ] `CategoryNotFoundException.java`
- [ ] `InvalidClassTimeException.java`
- [ ] `DuplicateClassTimeException.java`
- [ ] `InvalidImageException.java`

---

## 📝 파일 생성/수정 목록

### 수정할 파일 (1개)

1. `src/main/java/com/oneday/core/exception/ErrorCode.java`

### 생성할 파일 (4개)

1. `src/main/java/com/oneday/core/exception/classes/CategoryNotFoundException.java`
2. `src/main/java/com/oneday/core/exception/classes/InvalidClassTimeException.java`
3. `src/main/java/com/oneday/core/exception/classes/DuplicateClassTimeException.java`
4. `src/main/java/com/oneday/core/exception/classes/InvalidImageException.java`

---

## 🔧 구현 가이드

### 1. ErrorCode Enum 확장

**파일**: `src/main/java/com/oneday/core/exception/ErrorCode.java`

**추가할 에러 코드**:

```java
// 기존 코드...

// 클래스 등록 관련 에러 (CL001~CL008)
CATEGORY_NOT_FOUND(404,"CL001","존재하지 않는 카테고리입니다"),

INVALID_CLASS_TIME(400,"CL002","유효하지 않은 클래스 시간입니다"),

DUPLICATE_CLASS_TIME(409,"CL003","이미 등록된 시간대입니다"),

INVALID_IMAGE(400,"CL004","유효하지 않은 이미지입니다"),

INVALID_CLASS_NAME(400,"CL005","유효하지 않은 클래스명입니다"),

INVALID_PRICE(400,"CL006","유효하지 않은 가격입니다"),

INVALID_DESCRIPTION(400,"CL007","유효하지 않은 설명입니다"),

CLASS_NOT_FOUND(404,"CL008","존재하지 않는 클래스입니다");
```

**구현 포인트**:

- HTTP 상태 코드: 404 (Not Found), 400 (Bad Request), 409 (Conflict)
- 에러 코드: CL001~CL008 (PRD 명세 준수)
- 메시지: 한국어로 명확하게 작성

---

### 2. CategoryNotFoundException

**파일**: `src/main/java/com/oneday/core/exception/classes/CategoryNotFoundException.java`

```java
package com.oneday.core.exception.classes;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

/**
 * 카테고리를 찾을 수 없을 때 발생하는 예외
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public class CategoryNotFoundException extends CustomException {

  public CategoryNotFoundException(String message) {
    super(ErrorCode.CATEGORY_NOT_FOUND, message);
  }

  public CategoryNotFoundException() {
    super(ErrorCode.CATEGORY_NOT_FOUND, "존재하지 않는 카테고리입니다");
  }
}
```

**구현 포인트**:

- `CustomException` 상속
- `ErrorCode.CATEGORY_NOT_FOUND` 사용
- 기본 메시지와 커스텀 메시지 모두 지원
- Javadoc 주석 필수

---

### 3. InvalidClassTimeException

**파일**: `src/main/java/com/oneday/core/exception/classes/InvalidClassTimeException.java`

```java
package com.oneday.core.exception.classes;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

/**
 * 유효하지 않은 클래스 시간일 때 발생하는 예외
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public class InvalidClassTimeException extends CustomException {

  public InvalidClassTimeException(String message) {
    super(ErrorCode.INVALID_CLASS_TIME, message);
  }

  public InvalidClassTimeException() {
    super(ErrorCode.INVALID_CLASS_TIME, "유효하지 않은 클래스 시간입니다");
  }
}
```

**구현 포인트**:

- 시작 시간이 종료 시간보다 늦은 경우
- 과거 시간인 경우
- 시간 형식이 잘못된 경우

---

### 4. DuplicateClassTimeException

**파일**: `src/main/java/com/oneday/core/exception/classes/DuplicateClassTimeException.java`

```java
package com.oneday.core.exception.classes;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

/**
 * 동일한 강사의 동일한 시간대에 클래스가 이미 존재할 때 발생하는 예외
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public class DuplicateClassTimeException extends CustomException {

  public DuplicateClassTimeException(String message) {
    super(ErrorCode.DUPLICATE_CLASS_TIME, message);
  }

  public DuplicateClassTimeException() {
    super(ErrorCode.DUPLICATE_CLASS_TIME, "이미 등록된 시간대입니다");
  }
}
```

**구현 포인트**:

- 동일 강사의 시간 중복 체크
- 409 Conflict 상태 코드
- 명확한 메시지

---

### 5. InvalidImageException

**파일**: `src/main/java/com/oneday/core/exception/classes/InvalidImageException.java`

```java
package com.oneday.core.exception.classes;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

/**
 * 유효하지 않은 이미지일 때 발생하는 예외
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public class InvalidImageException extends CustomException {

  public InvalidImageException(String message) {
    super(ErrorCode.INVALID_IMAGE, message);
  }

  public InvalidImageException() {
    super(ErrorCode.INVALID_IMAGE, "유효하지 않은 이미지입니다");
  }
}
```

**구현 포인트**:

- 이미지 개수 검증 (1~5개)
- null 체크
- YAGNI 원칙: 파일 형식/크기 검증은 향후 작업

---

## 🧪 검증 방법

### 1. 컴파일 에러 확인

```bash
./gradlew compileJava
```

**예상 결과**: 에러 없이 컴파일 성공

### 2. ErrorCode Enum 확인

```java
// ErrorCode.java 테스트
System.out.println(ErrorCode.CATEGORY_NOT_FOUND.getCode()); // "CL001"
  System.out.

println(ErrorCode.CATEGORY_NOT_FOUND.getMessage()); // "존재하지 않는 카테고리입니다"
  System.out.

println(ErrorCode.CATEGORY_NOT_FOUND.getStatus()); // 404
```

### 3. 예외 클래스 생성 테스트

```java
// 테스트 코드 예시
CategoryNotFoundException e1 = new CategoryNotFoundException();
CategoryNotFoundException e2 = new CategoryNotFoundException("카테고리 ID 999를 찾을 수 없습니다");

assertEquals(ErrorCode.CATEGORY_NOT_FOUND, e1.getErrorCode());

assertEquals("카테고리 ID 999를 찾을 수 없습니다",e2.getMessage());
```

---

## 📋 예외 처리 전략

### GlobalExceptionHandler 통합

기존 `GlobalExceptionHandler.java`의 `handleCustomException()` 메서드가 자동으로 처리:

```java
@ExceptionHandler(CustomException.class)
public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
    log.error("CustomException: code={}, message={}", 
              e.getErrorCode().getCode(), e.getMessage());
    
    return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(ApiResponse.error(
                    e.getErrorCode().getCode(),
                    e.getMessage()
            ));
}
```

**예상 응답 예시**:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "CL001",
    "message": "존재하지 않는 카테고리입니다"
  }
}
```

---

## ⚠️ 주의사항

1. **패키지 구조**
  - `exception.classes` 패키지에 모든 클래스 예외 배치
  - `exception.auth`와 동일한 구조 유지

2. **Javadoc 주석**
  - 모든 예외 클래스에 필수
  - `@author`, `@since` 태그 포함

3. **메시지 일관성**
  - ErrorCode의 메시지와 예외 클래스의 기본 메시지 일치
  - 한국어로 명확하게 작성

4. **HTTP 상태 코드**
  - 404: Not Found (CATEGORY_NOT_FOUND, CLASS_NOT_FOUND)
  - 400: Bad Request (INVALID_*)
  - 409: Conflict (DUPLICATE_CLASS_TIME)

---

## 🔍 체크리스트

### 완료 조건

- [ ] ErrorCode Enum에 CL001~CL008 추가 완료
- [ ] exception.classes 패키지 생성 완료
- [ ] 4개 예외 클래스 생성 완료
- [ ] 컴파일 에러 없음 확인
- [ ] Javadoc 주석 모두 작성
- [ ] 기존 GlobalExceptionHandler와 통합 확인

---

## 🚀 다음 단계

Phase 3: Repository 계층으로 진행

**다음 작업**: `rules/plans/40-feature-register-class/phase-3-repository-layer.md` 참고

---

## 📊 진행 상황

- [x] Phase 2 작업 계획 수립
- [ ] Phase 2 구현 시작
- [ ] Phase 2 구현 완료
- [ ] Phase 2 검증 완료

