# Phase 4: Service 계층

## 🎯 목표

클래스 등록의 핵심 비즈니스 로직을 구현합니다.

**예상 소요 시간**: 2-3시간

---

## ✅ 작업 체크리스트

### 1. DTO 생성

- [ ] `dto.classes` 패키지 생성
- [ ] `CreateClassRequest.java` Record 생성
- [ ] `CreateClassResponse.java` Record 생성
- [ ] `TimeDto.java` Record 생성 (내부 DTO)
- [ ] `ImageDto.java` Record 생성 (내부 DTO)

### 2. ClassService 생성

- [ ] `service.classes` 패키지 생성
- [ ] `ClassService.java` 클래스 생성
- [ ] 의존성 주입 (Repository들)
- [ ] `createClass()` 메서드 구현

### 3. 비즈니스 로직 구현

- [ ] 카테고리 존재 검증
- [ ] 시간 유효성 검증
- [ ] 시간 중복 검증
- [ ] 이미지 개수 검증
- [ ] Classes 엔티티 생성 및 저장
- [ ] Times 엔티티 리스트 생성 및 저장
- [ ] Images 엔티티 리스트 생성 및 저장
- [ ] 트랜잭션 관리

---

## 📝 파일 생성/수정 목록

### 생성할 파일 (6개)

1. `src/main/java/com/oneday/core/dto/classes/CreateClassRequest.java`
2. `src/main/java/com/oneday/core/dto/classes/CreateClassResponse.java`
3. `src/main/java/com/oneday/core/dto/classes/TimeDto.java`
4. `src/main/java/com/oneday/core/dto/classes/ImageDto.java`
5. `src/main/java/com/oneday/core/service/classes/ClassService.java`

---

## 🔧 구현 가이드

### 1. TimeDto (내부 DTO)

**파일**: `src/main/java/com/oneday/core/dto/classes/TimeDto.java`

```java
package com.oneday.core.dto.classes;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 클래스 시간 정보 DTO
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public record TimeDto(
  @NotNull(message = "시작 시간은 필수입니다")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  LocalDateTime startTime,

  @NotNull(message = "종료 시간은 필수입니다")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  LocalDateTime endTime
) {
}
```

**구현 포인트**:

- Java Record 사용
- `@JsonFormat`으로 날짜 형식 명시 (ISO 8601)
- `@NotNull` 검증

---

### 2. ImageDto (내부 DTO)

**파일**: `src/main/java/com/oneday/core/dto/classes/ImageDto.java`

```java
package com.oneday.core.dto.classes;

import jakarta.validation.constraints.NotBlank;

/**
 * 클래스 이미지 정보 DTO
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public record ImageDto(
  @NotBlank(message = "이미지 URL은 필수입니다")
  String imageUrl
) {
}
```

**구현 포인트**:

- 단순한 구조 (YAGNI 원칙)
- `imageUrl`만 포함 (향후 확장 가능)

---

### 3. CreateClassRequest

**파일**: `src/main/java/com/oneday/core/dto/classes/CreateClassRequest.java`

```java
package com.oneday.core.dto.classes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * 클래스 등록 요청 DTO
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public record CreateClassRequest(
  @NotNull(message = "카테고리 ID는 필수입니다")
  @Positive(message = "카테고리 ID는 양수여야 합니다")
  Long categoryId,

  @NotBlank(message = "클래스명은 필수입니다")
  @Size(min = 1, max = 100, message = "클래스명은 1자 이상 100자 이하여야 합니다")
  String className,

  @NotNull(message = "가격은 필수입니다")
  @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
  Integer price,

  @NotBlank(message = "클래스 설명은 필수입니다")
  @Size(max = 1000, message = "클래스 설명은 1000자 이하여야 합니다")
  String description,

  @NotNull(message = "시간 정보는 필수입니다")
  @Size(min = 1, message = "최소 1개의 시간을 등록해야 합니다")
  @Valid
  List<TimeDto> times,

  @NotNull(message = "이미지 정보는 필수입니다")
  @Size(min = 1, max = 5, message = "이미지는 1개 이상 5개 이하여야 합니다")
  @Valid
  List<ImageDto> images
) {
}
```

**구현 포인트**:

- `@Valid`로 중첩 DTO 검증
- `@Size`로 리스트 개수 검증
- 모든 필드에 명확한 검증 메시지 (한국어)
- PRD 요구사항 준수:
  - className: 1~100자
  - price: 0원 이상
  - description: 최대 1000자
  - times: 최소 1개
  - images: 1~5개

---

### 4. CreateClassResponse

**파일**: `src/main/java/com/oneday/core/dto/classes/CreateClassResponse.java`

```java
package com.oneday.core.dto.classes;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 클래스 등록 응답 DTO
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public record CreateClassResponse(
  Long classId,
  String className,
  String categoryName,
  Integer price,
  String description,
  String instructorName,
  List<TimeInfo> times,
  List<String> imageUrls,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  LocalDateTime createdAt
) {
  /**
   * 시간 정보 (응답용)
   */
  public record TimeInfo(
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endTime
  ) {
  }
}
```

**구현 포인트**:

- 클라이언트에게 필요한 정보만 제공
- 중첩 Record로 시간 정보 표현
- 카테고리명, 강사명 포함 (조회 편의성)
- 생성 시간 포함

---

### 5. ClassService

**파일**: `src/main/java/com/oneday/core/service/classes/ClassService.java`

```java
package com.oneday.core.service.classes;

import com.oneday.core.dto.classes.CreateClassRequest;
import com.oneday.core.dto.classes.CreateClassResponse;
import com.oneday.core.dto.classes.ImageDto;
import com.oneday.core.dto.classes.TimeDto;
import com.oneday.core.entity.*;
import com.oneday.core.exception.classes.CategoryNotFoundException;
import com.oneday.core.exception.classes.DuplicateClassTimeException;
import com.oneday.core.exception.classes.InvalidClassTimeException;
import com.oneday.core.exception.classes.InvalidImageException;
import com.oneday.core.repository.CategoriesRepository;
import com.oneday.core.repository.ClassesRepository;
import com.oneday.core.repository.ImagesRepository;
import com.oneday.core.repository.TimesRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 클래스 관련 비즈니스 로직 처리
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassService {

  private final ClassesRepository classesRepository;
  private final CategoriesRepository categoriesRepository;
  private final TimesRepository timesRepository;
  private final ImagesRepository imagesRepository;

  /**
   * 클래스 등록
   *
   * @param user    인증된 강사 (INSTRUCTOR)
   * @param request 클래스 등록 요청 정보
   * @return 등록된 클래스 정보
   * @throws CategoryNotFoundException      카테고리를 찾을 수 없는 경우
   * @throws InvalidClassTimeException      유효하지 않은 시간인 경우
   * @throws DuplicateClassTimeException    중복된 시간대인 경우
   * @throws InvalidImageException          유효하지 않은 이미지인 경우
   */
  @Transactional
  public CreateClassResponse createClass(User user, CreateClassRequest request) {
    log.info("클래스 등록 시도: instructor={}, className={}", user.getEmail(), request.className());

    // 1. 카테고리 존재 확인
    Categories category = categoriesRepository.findById(request.categoryId())
      .orElseThrow(() -> {
        log.warn("존재하지 않는 카테고리: categoryId={}", request.categoryId());
        return new CategoryNotFoundException("카테고리 ID " + request.categoryId() + "를 찾을 수 없습니다");
      });

    // 2. 시간 유효성 검증
    validateTimes(request.times());

    // 3. 시간 중복 검증
    checkDuplicateTimes(user.getId(), request.times());

    // 4. 이미지 검증
    validateImages(request.images());

    // 5. Classes 엔티티 생성 및 저장
    Classes classes = Classes.builder()
      .user(user)
      .categories(category)
      .className(request.className())
      .price(request.price())
      .description(request.description())
      .build();
    Classes savedClass = classesRepository.save(classes);
    log.info("클래스 생성 완료: classId={}", savedClass.getId());

    // 6. Times 엔티티 리스트 생성 및 저장
    List<Times> timesList = request.times().stream()
      .map(timeDto -> Times.builder()
        .classes(savedClass)
        .startTime(timeDto.startTime())
        .endTime(timeDto.endTime())
        .build())
      .toList();
    timesRepository.saveAll(timesList);
    log.info("시간 정보 저장 완료: {} 개", timesList.size());

    // 7. Images 엔티티 리스트 생성 및 저장
    List<Images> imagesList = request.images().stream()
      .map(imageDto -> Images.builder()
        .classes(savedClass)
        .imageUrl(imageDto.imageUrl())
        .build())
      .toList();
    imagesRepository.saveAll(imagesList);
    log.info("이미지 정보 저장 완료: {} 개", imagesList.size());

    log.info("클래스 등록 완료: classId={}, instructor={}", savedClass.getId(), user.getEmail());

    // 8. 응답 DTO 생성
    return new CreateClassResponse(
      savedClass.getId(),
      savedClass.getClassName(),
      category.getName(),
      savedClass.getPrice(),
      savedClass.getDescription(),
      user.getName(),
      timesList.stream()
        .map(t -> new CreateClassResponse.TimeInfo(t.getStartTime(), t.getEndTime()))
        .toList(),
      imagesList.stream()
        .map(Images::getImageUrl)
        .toList(),
      savedClass.getCreatedAt()
    );
  }

  /**
   * 시간 유효성 검증
   */
  private void validateTimes(List<TimeDto> times) {
    LocalDateTime now = LocalDateTime.now();

    for (TimeDto timeDto : times) {
      // 시작 시간이 종료 시간보다 늦은 경우
      if (timeDto.startTime().isAfter(timeDto.endTime())) {
        log.warn("시작 시간이 종료 시간보다 늦음: start={}, end={}",
          timeDto.startTime(), timeDto.endTime());
        throw new InvalidClassTimeException("시작 시간은 종료 시간보다 빨라야 합니다");
      }

      // 과거 시간인 경우
      if (timeDto.startTime().isBefore(now)) {
        log.warn("과거 시간 등록 시도: startTime={}", timeDto.startTime());
        throw new InvalidClassTimeException("과거 시간은 등록할 수 없습니다");
      }
    }
  }

  /**
   * 시간 중복 검증
   */
  private void checkDuplicateTimes(Long userId, List<TimeDto> times) {
    for (TimeDto timeDto : times) {
      boolean exists = timesRepository.existsOverlappingTime(
        userId,
        timeDto.startTime(),
        timeDto.endTime()
      );

      if (exists) {
        log.warn("중복된 시간대 등록 시도: userId={}, start={}, end={}",
          userId, timeDto.startTime(), timeDto.endTime());
        throw new DuplicateClassTimeException(
          String.format("이미 등록된 시간대입니다: %s ~ %s",
            timeDto.startTime(), timeDto.endTime())
        );
      }
    }
  }

  /**
   * 이미지 검증
   */
  private void validateImages(List<ImageDto> images) {
    if (images == null || images.isEmpty()) {
      log.warn("이미지가 없음");
      throw new InvalidImageException("최소 1개의 이미지를 등록해야 합니다");
    }

    if (images.size() > 5) {
      log.warn("이미지 개수 초과: {} 개", images.size());
      throw new InvalidImageException("이미지는 최대 5개까지 등록할 수 있습니다");
    }

    for (ImageDto imageDto : images) {
      if (imageDto.imageUrl() == null || imageDto.imageUrl().isBlank()) {
        log.warn("유효하지 않은 이미지 URL");
        throw new InvalidImageException("유효하지 않은 이미지 URL입니다");
      }
    }
  }
}
```

**구현 포인트**:

- `@Transactional`: 클래스, 시간, 이미지가 모두 저장되거나 모두 롤백
- `@Transactional(readOnly = true)`: 클래스 레벨 (향후 조회 메서드 대비)
- 단계별 검증:
  1. 카테고리 존재 확인
  2. 시간 유효성 (시작 < 종료, 미래 시간)
  3. 시간 중복 확인
  4. 이미지 개수 (1~5개)
- 상세 로깅: 각 단계마다 로그 남김
- 명확한 예외 메시지

---

## 🧪 검증 방법

### 1. 컴파일 에러 확인

```bash
./gradlew compileJava
```

### 2. 애플리케이션 시작 확인

```bash
./gradlew bootRun
```

### 3. 수동 테스트 (Postman/cURL)

```bash
curl -X POST http://localhost:8080/api/classes \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "className": "요가 입문 클래스",
    "price": 50000,
    "description": "초보자를 위한 요가 클래스입니다",
    "times": [
      {
        "startTime": "2025-02-01T10:00:00",
        "endTime": "2025-02-01T12:00:00"
      }
    ],
    "images": [
      {
        "imageUrl": "https://example.com/image1.jpg"
      }
    ]
  }'
```

---

## ⚠️ 주의사항

1. **트랜잭션 관리**
  - `@Transactional` 필수 (데이터 무결성)
  - Classes, Times, Images가 원자적으로 저장

2. **검증 순서**
  - 카테고리 존재 → 시간 유효성 → 시간 중복 → 이미지
  - 빠른 실패 (Fail Fast) 원칙

3. **로깅**
  - 모든 검증 단계에서 `log.warn` 사용
  - 성공 시 `log.info` 사용

4. **예외 메시지**
  - 사용자에게 유의미한 메시지
  - 디버깅을 위한 상세 정보 포함

5. **YAGNI 원칙**
  - 파일 업로드는 구현하지 않음
  - 이미지 URL만 저장

---

## 🔍 체크리스트

### 완료 조건

- [ ] DTO 4개 생성 완료
- [ ] ClassService 생성 완료
- [ ] createClass() 메서드 구현 완료
- [ ] 비즈니스 검증 로직 모두 구현
- [ ] 트랜잭션 관리 확인
- [ ] 컴파일 에러 없음
- [ ] Javadoc 주석 모두 작성
- [ ] 로깅 추가 완료

---

## 🚀 다음 단계

Phase 5: Controller 계층으로 진행

**다음 작업**: `rules/plans/40-feature-register-class/phase-5-controller-layer.md` 참고

---

## 📊 진행 상황

- [x] Phase 4 작업 계획 수립
- [ ] Phase 4 구현 시작
- [ ] Phase 4 구현 완료
- [ ] Phase 4 검증 완료

