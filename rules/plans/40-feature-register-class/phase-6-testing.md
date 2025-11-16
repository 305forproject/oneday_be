# Phase 6: 테스트

## 🎯 목표

클래스 등록 기능의 단위 테스트와 통합 테스트를 작성합니다.

**예상 소요 시간**: 2-3시간

---

## ✅ 작업 체크리스트

### 1. ClassService 단위 테스트

- [ ] `ClassServiceTest.java` 클래스 생성
- [ ] Mock 객체 설정 (Repository들)
- [ ] 7개 테스트 시나리오 구현

### 2. ClassController 통합 테스트

- [ ] `ClassControllerTest.java` 클래스 생성
- [ ] MockMvc 설정
- [ ] 인증/권한 테스트
- [ ] API 통합 테스트

---

## 📝 파일 생성/수정 목록

### 생성할 파일 (2개)

1. `src/test/java/com/oneday/core/service/classes/ClassServiceTest.java`
2. `src/test/java/com/oneday/core/controller/classes/ClassControllerTest.java`

---

## 🔧 구현 가이드

### 1. ClassServiceTest (단위 테스트)

**파일**: `src/test/java/com/oneday/core/service/classes/ClassServiceTest.java`

```java
package com.oneday.core.service.classes;

import com.oneday.core.dto.classes.*;
import com.oneday.core.entity.*;
import com.oneday.core.exception.classes.*;
import com.oneday.core.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ClassService 단위 테스트
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("클래스 서비스 테스트")
class ClassServiceTest {

  @Mock
  private ClassesRepository classesRepository;

  @Mock
  private CategoriesRepository categoriesRepository;

  @Mock
  private TimesRepository timesRepository;

  @Mock
  private ImagesRepository imagesRepository;

  @InjectMocks
  private ClassService classService;

  private User instructor;
  private Categories category;
  private CreateClassRequest validRequest;

  @BeforeEach
  void setUp() {
    // 테스트용 강사 생성
    instructor = User.builder()
      .email("instructor@example.com")
      .password("encodedPassword")
      .name("강사명")
      .role(Role.INSTRUCTOR)
      .build();

    // 테스트용 카테고리 생성
    category = Categories.builder()
      .name("건강/뷰티")
      .build();

    // 유효한 요청 DTO
    validRequest = new CreateClassRequest(
      1L,
      "요가 입문 클래스",
      50000,
      "초보자를 위한 요가 클래스입니다",
      List.of(new TimeDto(
        LocalDateTime.of(2025, 2, 1, 10, 0),
        LocalDateTime.of(2025, 2, 1, 12, 0)
      )),
      List.of(new ImageDto("https://example.com/image1.jpg"))
    );
  }

  @Test
  @DisplayName("클래스 등록 - 성공")
  void createClass_Success() {
    // given
    when(categoriesRepository.findById(1L)).thenReturn(Optional.of(category));
    when(timesRepository.existsOverlappingTime(any(), any(), any())).thenReturn(false);

    Classes savedClass = Classes.builder()
      .user(instructor)
      .categories(category)
      .className("요가 입문 클래스")
      .price(50000)
      .description("초보자를 위한 요가 클래스입니다")
      .build();
    // Reflection으로 ID 설정 (테스트용)
    setId(savedClass, 1L);

    when(classesRepository.save(any(Classes.class))).thenReturn(savedClass);
    when(timesRepository.saveAll(any())).thenReturn(List.of());
    when(imagesRepository.saveAll(any())).thenReturn(List.of());

    // when
    CreateClassResponse response = classService.createClass(instructor, validRequest);

    // then
    assertThat(response).isNotNull();
    assertThat(response.classId()).isEqualTo(1L);
    assertThat(response.className()).isEqualTo("요가 입문 클래스");
    assertThat(response.categoryName()).isEqualTo("건강/뷰티");
    assertThat(response.price()).isEqualTo(50000);
    assertThat(response.instructorName()).isEqualTo("강사명");
    assertThat(response.times()).hasSize(1);
    assertThat(response.imageUrls()).hasSize(1);

    verify(categoriesRepository, times(1)).findById(1L);
    verify(classesRepository, times(1)).save(any(Classes.class));
    verify(timesRepository, times(1)).saveAll(any());
    verify(imagesRepository, times(1)).saveAll(any());
  }

  @Test
  @DisplayName("클래스 등록 - 실패: 카테고리 미존재")
  void createClass_Fail_CategoryNotFound() {
    // given
    when(categoriesRepository.findById(1L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> classService.createClass(instructor, validRequest))
      .isInstanceOf(CategoryNotFoundException.class)
      .hasMessageContaining("카테고리 ID 1를 찾을 수 없습니다");

    verify(categoriesRepository, times(1)).findById(1L);
    verify(classesRepository, never()).save(any());
  }

  @Test
  @DisplayName("클래스 등록 - 실패: 시작 시간이 종료 시간보다 늦음")
  void createClass_Fail_InvalidTime_StartAfterEnd() {
    // given
    CreateClassRequest invalidRequest = new CreateClassRequest(
      1L,
      "요가 입문 클래스",
      50000,
      "설명",
      List.of(new TimeDto(
        LocalDateTime.of(2025, 2, 1, 12, 0),  // 종료 시간
        LocalDateTime.of(2025, 2, 1, 10, 0)   // 시작 시간
      )),
      List.of(new ImageDto("https://example.com/image1.jpg"))
    );

    when(categoriesRepository.findById(1L)).thenReturn(Optional.of(category));

    // when & then
    assertThatThrownBy(() -> classService.createClass(instructor, invalidRequest))
      .isInstanceOf(InvalidClassTimeException.class)
      .hasMessageContaining("시작 시간은 종료 시간보다 빨라야 합니다");

    verify(classesRepository, never()).save(any());
  }

  @Test
  @DisplayName("클래스 등록 - 실패: 과거 시간")
  void createClass_Fail_InvalidTime_Past() {
    // given
    CreateClassRequest pastRequest = new CreateClassRequest(
      1L,
      "요가 입문 클래스",
      50000,
      "설명",
      List.of(new TimeDto(
        LocalDateTime.of(2020, 1, 1, 10, 0),  // 과거
        LocalDateTime.of(2020, 1, 1, 12, 0)
      )),
      List.of(new ImageDto("https://example.com/image1.jpg"))
    );

    when(categoriesRepository.findById(1L)).thenReturn(Optional.of(category));

    // when & then
    assertThatThrownBy(() -> classService.createClass(instructor, pastRequest))
      .isInstanceOf(InvalidClassTimeException.class)
      .hasMessageContaining("과거 시간은 등록할 수 없습니다");

    verify(classesRepository, never()).save(any());
  }

  @Test
  @DisplayName("클래스 등록 - 실패: 시간 중복")
  void createClass_Fail_DuplicateTime() {
    // given
    when(categoriesRepository.findById(1L)).thenReturn(Optional.of(category));
    when(timesRepository.existsOverlappingTime(any(), any(), any())).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> classService.createClass(instructor, validRequest))
      .isInstanceOf(DuplicateClassTimeException.class)
      .hasMessageContaining("이미 등록된 시간대입니다");

    verify(classesRepository, never()).save(any());
  }

  @Test
  @DisplayName("클래스 등록 - 실패: 이미지 없음")
  void createClass_Fail_NoImages() {
    // given
    CreateClassRequest noImageRequest = new CreateClassRequest(
      1L,
      "요가 입문 클래스",
      50000,
      "설명",
      List.of(new TimeDto(
        LocalDateTime.of(2025, 2, 1, 10, 0),
        LocalDateTime.of(2025, 2, 1, 12, 0)
      )),
      List.of()  // 빈 리스트
    );

    when(categoriesRepository.findById(1L)).thenReturn(Optional.of(category));
    when(timesRepository.existsOverlappingTime(any(), any(), any())).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> classService.createClass(instructor, noImageRequest))
      .isInstanceOf(InvalidImageException.class)
      .hasMessageContaining("최소 1개의 이미지를 등록해야 합니다");

    verify(classesRepository, never()).save(any());
  }

  @Test
  @DisplayName("클래스 등록 - 실패: 이미지 개수 초과")
  void createClass_Fail_TooManyImages() {
    // given
    CreateClassRequest tooManyImagesRequest = new CreateClassRequest(
      1L,
      "요가 입문 클래스",
      50000,
      "설명",
      List.of(new TimeDto(
        LocalDateTime.of(2025, 2, 1, 10, 0),
        LocalDateTime.of(2025, 2, 1, 12, 0)
      )),
      List.of(
        new ImageDto("url1"),
        new ImageDto("url2"),
        new ImageDto("url3"),
        new ImageDto("url4"),
        new ImageDto("url5"),
        new ImageDto("url6")  // 6개 (초과)
      )
    );

    when(categoriesRepository.findById(1L)).thenReturn(Optional.of(category));
    when(timesRepository.existsOverlappingTime(any(), any(), any())).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> classService.createClass(instructor, tooManyImagesRequest))
      .isInstanceOf(InvalidImageException.class)
      .hasMessageContaining("이미지는 최대 5개까지 등록할 수 있습니다");

    verify(classesRepository, never()).save(any());
  }

  /**
   * Reflection으로 ID 설정 (테스트용 헬퍼 메서드)
   */
  private void setId(Classes classes, Long id) {
    try {
      var field = Classes.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(classes, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
```

**구현 포인트**:

- `@ExtendWith(MockitoExtension.class)`: Mockito 사용
- `@Mock`: 의존성 모킹
- `@InjectMocks`: 테스트 대상 (ClassService)
- Given-When-Then 패턴
- AssertJ 단언문 (`assertThat`, `assertThatThrownBy`)
- `verify()`: Mock 호출 검증
- 7개 시나리오 커버:
  1. 성공
  2. 카테고리 미존재
  3. 시작 시간 > 종료 시간
  4. 과거 시간
  5. 시간 중복
  6. 이미지 없음
  7. 이미지 초과

---

### 2. ClassControllerTest (통합 테스트)

**파일**: `src/test/java/com/oneday/core/controller/classes/ClassControllerTest.java`

```java
package com.oneday.core.controller.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneday.core.config.security.JwtTokenProvider;
import com.oneday.core.dto.classes.CreateClassRequest;
import com.oneday.core.dto.classes.ImageDto;
import com.oneday.core.dto.classes.TimeDto;
import com.oneday.core.entity.Categories;
import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;
import com.oneday.core.repository.CategoriesRepository;
import com.oneday.core.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ClassController 통합 테스트
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("클래스 컨트롤러 통합 테스트")
class ClassControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CategoriesRepository categoriesRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private String instructorToken;
  private String userToken;
  private User instructor;
  private User normalUser;
  private Categories category;

  @BeforeEach
  void setUp() {
    // 강사 생성
    instructor = User.builder()
      .email("instructor@example.com")
      .password(passwordEncoder.encode("password123"))
      .name("강사명")
      .role(Role.INSTRUCTOR)
      .build();
    instructor = userRepository.save(instructor);

    // 일반 사용자 생성
    normalUser = User.builder()
      .email("user@example.com")
      .password(passwordEncoder.encode("password123"))
      .name("일반사용자")
      .role(Role.USER)
      .build();
    normalUser = userRepository.save(normalUser);

    // JWT 토큰 생성
    instructorToken = jwtTokenProvider.generateAccessToken(instructor.getEmail());
    userToken = jwtTokenProvider.generateAccessToken(normalUser.getEmail());

    // 카테고리 생성
    category = Categories.builder()
      .name("건강/뷰티")
      .build();
    category = categoriesRepository.save(category);
  }

  @Test
  @DisplayName("클래스 등록 - 성공 (INSTRUCTOR 권한)")
  void createClass_Success_WithInstructorRole() throws Exception {
    // given
    CreateClassRequest request = new CreateClassRequest(
      category.getId(),
      "요가 입문 클래스",
      50000,
      "초보자를 위한 요가 클래스입니다",
      List.of(new TimeDto(
        LocalDateTime.of(2025, 2, 1, 10, 0),
        LocalDateTime.of(2025, 2, 1, 12, 0)
      )),
      List.of(new ImageDto("https://example.com/image1.jpg"))
    );

    // when & then
    mockMvc.perform(post("/api/classes")
        .header("Authorization", "Bearer " + instructorToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.className").value("요가 입문 클래스"))
      .andExpect(jsonPath("$.data.categoryName").value("건강/뷰티"))
      .andExpect(jsonPath("$.data.price").value(50000))
      .andExpect(jsonPath("$.data.instructorName").value("강사명"))
      .andExpect(jsonPath("$.data.times").isArray())
      .andExpect(jsonPath("$.data.imageUrls").isArray())
      .andExpect(jsonPath("$.error").isEmpty());
  }

  @Test
  @DisplayName("클래스 등록 - 실패: 인증 없음")
  void createClass_Fail_Unauthorized() throws Exception {
    // given
    CreateClassRequest request = new CreateClassRequest(
      category.getId(),
      "요가 입문 클래스",
      50000,
      "설명",
      List.of(new TimeDto(
        LocalDateTime.of(2025, 2, 1, 10, 0),
        LocalDateTime.of(2025, 2, 1, 12, 0)
      )),
      List.of(new ImageDto("https://example.com/image1.jpg"))
    );

    // when & then
    mockMvc.perform(post("/api/classes")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("클래스 등록 - 실패: USER 권한 (403 Forbidden)")
  void createClass_Fail_Forbidden_UserRole() throws Exception {
    // given
    CreateClassRequest request = new CreateClassRequest(
      category.getId(),
      "요가 입문 클래스",
      50000,
      "설명",
      List.of(new TimeDto(
        LocalDateTime.of(2025, 2, 1, 10, 0),
        LocalDateTime.of(2025, 2, 1, 12, 0)
      )),
      List.of(new ImageDto("https://example.com/image1.jpg"))
    );

    // when & then
    mockMvc.perform(post("/api/classes")
        .header("Authorization", "Bearer " + userToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("클래스 등록 - 실패: 빈 클래스명 (검증 실패)")
  void createClass_Fail_EmptyClassName() throws Exception {
    // given
    CreateClassRequest request = new CreateClassRequest(
      category.getId(),
      "",  // 빈 값
      50000,
      "설명",
      List.of(new TimeDto(
        LocalDateTime.of(2025, 2, 1, 10, 0),
        LocalDateTime.of(2025, 2, 1, 12, 0)
      )),
      List.of(new ImageDto("https://example.com/image1.jpg"))
    );

    // when & then
    mockMvc.perform(post("/api/classes")
        .header("Authorization", "Bearer " + instructorToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("클래스 등록 - 실패: 존재하지 않는 카테고리")
  void createClass_Fail_CategoryNotFound() throws Exception {
    // given
    CreateClassRequest request = new CreateClassRequest(
      999L,  // 존재하지 않는 ID
      "요가 입문 클래스",
      50000,
      "설명",
      List.of(new TimeDto(
        LocalDateTime.of(2025, 2, 1, 10, 0),
        LocalDateTime.of(2025, 2, 1, 12, 0)
      )),
      List.of(new ImageDto("https://example.com/image1.jpg"))
    );

    // when & then
    mockMvc.perform(post("/api/classes")
        .header("Authorization", "Bearer " + instructorToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.success").value(false))
      .andExpect(jsonPath("$.error.code").value("CL001"));
  }
}
```

**구현 포인트**:

- `@SpringBootTest`: 전체 Spring Context 로드
- `@AutoConfigureMockMvc`: MockMvc 자동 설정
- `@Transactional`: 각 테스트 후 롤백
- MockMvc: HTTP 요청 시뮬레이션
- JWT 토큰 생성 및 사용
- 실제 DB 사용 (H2 또는 MySQL)
- 5개 시나리오:
  1. 성공 (INSTRUCTOR)
  2. 인증 없음 (401)
  3. USER 권한 (403)
  4. 검증 실패 (400)
  5. 카테고리 미존재 (404)

---

## 🧪 검증 방법

### 1. 전체 테스트 실행

```bash
./gradlew test
```

**예상 결과**: 모든 테스트 통과

### 2. 특정 테스트 클래스 실행

```bash
./gradlew test --tests ClassServiceTest
./gradlew test --tests ClassControllerTest
```

### 3. 테스트 커버리지 확인

```bash
./gradlew test jacocoTestReport
```

**리포트 위치**: `build/reports/jacoco/test/html/index.html`

---

## ⚠️ 주의사항

1. **테스트 독립성**
  - 각 테스트는 독립적으로 실행 가능
  - `@BeforeEach`로 데이터 초기화
  - `@Transactional`로 테스트 후 롤백

2. **Given-When-Then 패턴**
  - 명확한 구조로 가독성 향상
  - 주석으로 구분

3. **예외 테스트**
  - `assertThatThrownBy()` 사용
  - 예외 타입과 메시지 모두 검증

4. **Mock 검증**
  - `verify()`: 메서드 호출 여부 확인
  - `never()`: 호출되지 않아야 할 메서드 검증

5. **통합 테스트 DB**
  - 실제 DB 사용 (테스트용)
  - `@Transactional`로 데이터 정리

---

## 🔍 체크리스트

### 완료 조건

- [ ] ClassServiceTest 생성 완료
- [ ] 단위 테스트 7개 구현 완료
- [ ] ClassControllerTest 생성 완료
- [ ] 통합 테스트 5개 구현 완료
- [ ] 모든 테스트 통과 확인
- [ ] Given-When-Then 패턴 준수
- [ ] Javadoc 주석 모두 작성
- [ ] `@DisplayName` 한국어 작성

---

## 🎉 최종 검증

### 전체 시나리오 테스트 통과 확인

```bash
./gradlew clean test
```

**예상 결과**:

```
ClassServiceTest
  ✓ 클래스 등록 - 성공
  ✓ 클래스 등록 - 실패: 카테고리 미존재
  ✓ 클래스 등록 - 실패: 시작 시간이 종료 시간보다 늦음
  ✓ 클래스 등록 - 실패: 과거 시간
  ✓ 클래스 등록 - 실패: 시간 중복
  ✓ 클래스 등록 - 실패: 이미지 없음
  ✓ 클래스 등록 - 실패: 이미지 개수 초과

ClassControllerTest
  ✓ 클래스 등록 - 성공 (INSTRUCTOR 권한)
  ✓ 클래스 등록 - 실패: 인증 없음
  ✓ 클래스 등록 - 실패: USER 권한 (403 Forbidden)
  ✓ 클래스 등록 - 실패: 빈 클래스명 (검증 실패)
  ✓ 클래스 등록 - 실패: 존재하지 않는 카테고리

BUILD SUCCESSFUL
12 tests completed, 12 succeeded
```

---

## 🚀 완료!

**축하합니다!** 클래스 등록 기능의 모든 Phase가 완료되었습니다.

### 다음 작업

- [ ] PR 생성 및 코드 리뷰 요청
- [ ] main 브랜치로 병합
- [ ] 배포 준비

---

## 📊 진행 상황

- [x] Phase 6 작업 계획 수립
- [ ] Phase 6 구현 시작
- [ ] Phase 6 구현 완료
- [ ] Phase 6 검증 완료
- [ ] **전체 기능 완료**

