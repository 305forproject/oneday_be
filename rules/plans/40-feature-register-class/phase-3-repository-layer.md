# Phase 3: Repository 계층

## 🎯 목표

데이터 접근 계층(Repository)을 구현하여 Classes, Times, Images 엔티티의 CRUD 및 비즈니스 쿼리를 지원합니다.

**예상 소요 시간**: 1시간

---

## ✅ 작업 체크리스트

### 1. ClassesRepository 생성

- [ ] `ClassesRepository.java` 인터페이스 생성
- [ ] JpaRepository 상속
- [ ] 강사별 클래스 조회 메서드 정의

### 2. TimesRepository 생성

- [ ] `TimesRepository.java` 인터페이스 생성
- [ ] JpaRepository 상속
- [ ] 시간 중복 체크 쿼리 메서드 정의

### 3. ImagesRepository 생성

- [ ] `ImagesRepository.java` 인터페이스 생성
- [ ] JpaRepository 상속

### 4. 기존 Repository 확인

- [ ] `CategoriesRepository.java` 존재 확인

---

## 📝 파일 생성/수정 목록

### 생성할 파일 (3개)

1. `src/main/java/com/oneday/core/repository/ClassesRepository.java`
2. `src/main/java/com/oneday/core/repository/TimesRepository.java`
3. `src/main/java/com/oneday/core/repository/ImagesRepository.java`

### 수정할 파일 (없음)

기존 Repository는 수정하지 않습니다.

---

## 🔧 구현 가이드

### 1. ClassesRepository

**파일**: `src/main/java/com/oneday/core/repository/ClassesRepository.java`

```java
package com.oneday.core.repository;

import com.oneday.core.entity.Classes;
import com.oneday.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Classes 엔티티 Repository
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Repository
public interface ClassesRepository extends JpaRepository<Classes, Long> {
    
    /**
     * 특정 강사의 모든 클래스 조회
     * @param user 강사 (User 엔티티)
     * @return 클래스 목록
     */
    List<Classes> findByUser(User user);
}
```

**구현 포인트**:

- `JpaRepository<Classes, Long>` 상속
- Spring Data JPA 메서드 네이밍 규칙 준수
- `findByUser()`: 향후 강사별 클래스 목록 조회 시 사용 (YAGNI 준수)
- `@Repository` 어노테이션 추가

---

### 2. TimesRepository

**파일**: `src/main/java/com/oneday/core/repository/TimesRepository.java`

```java
package com.oneday.core.repository;

import com.oneday.core.entity.Classes;
import com.oneday.core.entity.Times;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Times 엔티티 Repository
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Repository
public interface TimesRepository extends JpaRepository<Times, Long> {

  /**
   * 특정 강사의 특정 시간대에 중복된 클래스가 있는지 확인
   *
   * @param userId 강사 ID
   * @param startTime 시작 시간
   * @param endTime 종료 시간
   * @return 중복된 시간대가 있으면 true
   */
  @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM Times t
        WHERE t.classes.user.id = :userId
        AND (
            (t.startTime < :endTime AND t.endTime > :startTime)
        )
    """)
  boolean existsOverlappingTime(
    @Param("userId") Long userId,
    @Param("startTime") LocalDateTime startTime,
    @Param("endTime") LocalDateTime endTime
  );

  /**
   * 특정 클래스의 모든 시간 조회
   * @param classes 클래스 엔티티
   * @return 시간 목록
   */
  List<Times> findByClasses(Classes classes);
}
```

**구현 포인트**:

- `@Query` 어노테이션으로 복잡한 시간 중복 체크 구현
- JPQL (Java Persistence Query Language) 사용
- 시간 중복 로직:
  - 새 시작 시간이 기존 종료 시간보다 이전이고
  - 새 종료 시간이 기존 시작 시간보다 이후인 경우
- `boolean` 반환 타입으로 존재 여부만 체크 (효율적)
- `@Param`으로 파라미터 명시

**시간 중복 예시**:

```
기존: |-------|  (10:00 ~ 12:00)
새로: |-------|  (11:00 ~ 13:00) → 중복!

기존: |-------|  (10:00 ~ 12:00)
새로:          |-------| (13:00 ~ 15:00) → 중복 아님
```

---

### 3. ImagesRepository

**파일**: `src/main/java/com/oneday/core/repository/ImagesRepository.java`

```java
package com.oneday.core.repository;

import com.oneday.core.entity.Classes;
import com.oneday.core.entity.Images;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Images 엔티티 Repository
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Repository
public interface ImagesRepository extends JpaRepository<Images, Long> {

  /**
   * 특정 클래스의 모든 이미지 조회
   * @param classes 클래스 엔티티
   * @return 이미지 목록
   */
  List<Images> findByClasses(Classes classes);
}
```

**구현 포인트**:

- 기본 CRUD 제공 (JpaRepository 상속)
- `findByClasses()`: 향후 클래스 상세 조회 시 사용
- 단순한 구조 (YAGNI 원칙)

---

## 🧪 검증 방법

### 1. 컴파일 에러 확인

```bash
./gradlew compileJava
```

**예상 결과**: 에러 없이 컴파일 성공

### 2. Repository Bean 로딩 확인

```bash
./gradlew bootRun
```

**예상 로그**:

```
... Mapped "{[/api]}" ...
... JpaRepository bean created: classesRepository
... JpaRepository bean created: timesRepository
... JpaRepository bean created: imagesRepository
```

### 3. 시간 중복 쿼리 테스트 (향후 Phase 6에서)

```java
@Test
void existsOverlappingTime_중복있음() {
    // given
    User user = createUser();
    Classes classes = createClass(user);
    Times time = Times.builder()
            .classes(classes)
            .startTime(LocalDateTime.of(2025, 2, 1, 10, 0))
            .endTime(LocalDateTime.of(2025, 2, 1, 12, 0))
            .build();
    timesRepository.save(time);
    
    // when
    boolean exists = timesRepository.existsOverlappingTime(
            user.getId(),
            LocalDateTime.of(2025, 2, 1, 11, 0),
            LocalDateTime.of(2025, 2, 1, 13, 0)
    );
    
    // then
    assertTrue(exists);
}
```

---

## 📋 JPQL 쿼리 분석

### existsOverlappingTime 쿼리

```sql
SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
FROM Times t
WHERE t.classes.user.id = :userId
  AND (
  (t.startTime < :endTime AND t.endTime > :startTime)
  )
```

**쿼리 설명**:

1. `FROM Times t`: Times 테이블 조회
2. `t.classes.user.id = :userId`: 동일 강사의 클래스만 필터링
3. `t.startTime < :endTime AND t.endTime > :startTime`: 시간 중복 조건
4. `COUNT(t) > 0`: 중복이 1개라도 있으면 true

**성능 고려**:

- 인덱스: `classes_id`, `start_time`, `end_time` (향후 추가 고려)
- 현재 단계에서는 YAGNI 원칙에 따라 인덱스 추가하지 않음

---

## ⚠️ 주의사항

1. **Repository 네이밍**
  - `{Entity}Repository` 형식 준수
  - 예: `ClassesRepository`, `TimesRepository`

2. **메서드 네이밍**
  - Spring Data JPA 규칙 준수
  - `findBy`, `existsBy`, `countBy` 등

3. **JPQL vs Native Query**
  - 현재는 JPQL 사용 (데이터베이스 독립적)
  - 복잡한 쿼리가 필요하면 Native Query 고려 (향후)

4. **@Repository 어노테이션**
  - 명시적으로 추가 (가독성)
  - Spring Data JPA가 자동으로 인식하지만 명시 권장

5. **연관관계 조회**
  - `findByClasses()`: JPA가 자동으로 JOIN 수행
  - N+1 문제 주의 (향후 `@EntityGraph` 고려)

---

## 🔍 체크리스트

### 완료 조건

- [ ] ClassesRepository 생성 완료
- [ ] TimesRepository 생성 완료
- [ ] ImagesRepository 생성 완료
- [ ] 컴파일 에러 없음 확인
- [ ] Javadoc 주석 모두 작성
- [ ] 애플리케이션 정상 시작 확인
- [ ] Repository Bean 로딩 확인

---

## 🚀 다음 단계

Phase 4: Service 계층으로 진행

**다음 작업**: `rules/plans/40-feature-register-class/phase-4-service-layer.md` 참고

---

## 📊 진행 상황

- [x] Phase 3 작업 계획 수립
- [ ] Phase 3 구현 시작
- [ ] Phase 3 구현 완료
- [ ] Phase 3 검증 완료

