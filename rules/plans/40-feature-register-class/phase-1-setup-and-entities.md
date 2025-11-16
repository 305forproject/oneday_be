# Phase 1: 기본 설정 및 엔티티 준비

## 🎯 목표

CategoryType Enum을 생성하고 Categories 테이블을 자동으로 초기화합니다.

**예상 소요 시간**: 1시간

---

## ✅ 작업 체크리스트

### 1. CategoryType Enum 생성

- [ ] `com.oneday.core.entity.CategoryType.java` 파일 생성
- [ ] 8개 카테고리 정의
- [ ] 한글명과 영문명 필드 추가
- [ ] Javadoc 주석 작성

### 2. CategoryInitializer 생성

- [ ] `com.oneday.core.config.CategoryInitializer.java` 파일 생성
- [ ] `@PostConstruct`로 Categories 테이블 초기화
- [ ] 중복 초기화 방지 로직 추가
- [ ] 로깅 추가

### 3. 기존 엔티티 검증

- [ ] `Classes.java` 연관관계 확인
- [ ] `Times.java` 연관관계 확인
- [ ] `Images.java` 연관관계 확인
- [ ] `Categories.java` 구조 확인

---

## 📝 파일 생성/수정 목록

### 생성할 파일 (2개)

1. `src/main/java/com/oneday/core/entity/CategoryType.java`
2. `src/main/java/com/oneday/core/config/CategoryInitializer.java`

### 수정할 파일 (없음)

기존 엔티티는 수정하지 않습니다 (YAGNI 원칙).

---

## 🔧 구현 가이드

### 1. CategoryType Enum

```java
package com.oneday.core.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 클래스 카테고리 타입
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Getter
@RequiredArgsConstructor
public enum CategoryType {
  HEALTH_BEAUTY("건강/뷰티", "Health & Beauty"),
  CRAFT_ART("공예/예술", "Craft & Art"),
  SPORTS_LEISURE("스포츠/레저", "Sports & Leisure"),
  COOKING_BAKING("요리/베이킹", "Cooking & Baking"),
  MUSIC_DANCE("음악/댄스", "Music & Dance"),
  LANGUAGE_EDUCATION("언어/교육", "Language & Education"),
  IT_TECHNOLOGY("IT/기술", "IT & Technology"),
  LIFESTYLE("라이프스타일", "Lifestyle");

  private final String koreanName;
  private final String englishName;
}
```

**구현 포인트**:

- `@Getter`로 필드 접근자 자동 생성
- `@RequiredArgsConstructor`로 생성자 자동 생성
- 한글명과 영문명 모두 제공 (향후 다국어 대응 고려)
- Javadoc 주석 필수

---

### 2. CategoryInitializer

```java
package com.oneday.core.config;

import com.oneday.core.entity.Categories;
import com.oneday.core.entity.CategoryType;
import com.oneday.core.repository.CategoriesRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * 애플리케이션 시작 시 Categories 테이블 초기화
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryInitializer {

  private final CategoriesRepository categoriesRepository;

  /**
   * Categories 테이블이 비어있으면 초기 데이터 삽입
   */
  @PostConstruct
  @Transactional
  public void initCategories() {
    if (categoriesRepository.count() == 0) {
      log.info("Categories 테이블 초기화 시작");

      Arrays.stream(CategoryType.values())
        .forEach(type -> {
          Categories category = Categories.builder()
            .name(type.getKoreanName())
            .build();
          categoriesRepository.save(category);
          log.info("카테고리 등록: {}", type.getKoreanName());
        });

      log.info("Categories 테이블 초기화 완료: {} 개", CategoryType.values().length);
    } else {
      log.info("Categories 테이블 이미 초기화됨: {} 개", categoriesRepository.count());
    }
  }
}
```

**구현 포인트**:

- `@PostConstruct`: 빈 생성 후 자동 실행
- `@Transactional`: 트랜잭션 보장
- 중복 초기화 방지: `count() == 0` 체크
- 상세 로깅: 초기화 과정 추적 가능

---

## 🧪 검증 방법

### 1. 애플리케이션 시작 후 로그 확인

```bash
./gradlew bootRun
```

**예상 로그**:

```
INFO  c.o.c.config.CategoryInitializer - Categories 테이블 초기화 시작
INFO  c.o.c.config.CategoryInitializer - 카테고리 등록: 건강/뷰티
INFO  c.o.c.config.CategoryInitializer - 카테고리 등록: 공예/예술
...
INFO  c.o.c.config.CategoryInitializer - Categories 테이블 초기화 완료: 8 개
```

### 2. MySQL 데이터 확인

```sql
SELECT *
FROM categories;
```

**예상 결과**: 8개 행이 존재해야 함

### 3. 재시작 시 로그 확인

```
INFO  c.o.c.config.CategoryInitializer - Categories 테이블 이미 초기화됨: 8 개
```

---

## ⚠️ 주의사항

1. **Categories 엔티티 확인**

- `Categories.java`에 `name` 필드가 있는지 확인
- `@Builder` 어노테이션이 있는지 확인

2. **CategoriesRepository 존재 확인**

- `src/main/java/com/oneday/core/repository/CategoriesRepository.java` 파일 존재 확인
- 없으면 생성 필요:
  ```java
  public interface CategoriesRepository extends JpaRepository<Categories, Long> {
  }
  ```

3. **트랜잭션 관리**

- `@Transactional`이 반드시 필요 (데이터 무결성)

4. **로깅 레벨**

- `log.info` 사용 (중요한 초기화 작업)

---

## 🔍 체크리스트

### 완료 조건

- [x] CategoryType Enum 생성 완료
- [x] CategoryInitializer 생성 완료
- [x] 애플리케이션 정상 시작
- [x] 로그에 "Categories 테이블 초기화 완료" 메시지 확인
- [x] MySQL에서 categories 테이블에 8개 행 확인
- [x] 재시작 시 중복 초기화되지 않음 확인
- [x] 기존 엔티티 연관관계 검증 완료

---

## 🚀 다음 단계

Phase 2: 예외 처리 구조로 진행

**다음 작업**: `rules/plans/40-feature-register-class/phase-2-exception-handling.md` 참고

---

## 📊 진행 상황

- [x] Phase 1 작업 계획 수립
- [x] Phase 1 구현 시작
- [x] Phase 1 구현 완료
- [x] Phase 1 검증 완료 ✅

**검증 결과:**

- Categories 테이블에 8개 카테고리 데이터 확인 완료
- 중복 초기화 방지 로직 정상 작동 확인

