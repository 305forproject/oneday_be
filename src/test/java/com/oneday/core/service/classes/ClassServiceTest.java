package com.oneday.core.service.classes;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oneday.core.dto.classes.ImageDto;
import com.oneday.core.dto.classes.RegisterClassRequest;
import com.oneday.core.dto.classes.RegisterClassResponse;
import com.oneday.core.entity.Categories;
import com.oneday.core.entity.CategoryType;
import com.oneday.core.entity.Classes;
import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;
import com.oneday.core.exception.classes.CategoryNotFoundException;
import com.oneday.core.exception.classes.DuplicateClassTimeException;
import com.oneday.core.exception.classes.InvalidClassTimeException;
import com.oneday.core.exception.classes.InvalidImageException;
import com.oneday.core.repository.CategoriesRepository;
import com.oneday.core.repository.ClassesRepository;
import com.oneday.core.repository.ImageRepository;
import com.oneday.core.repository.TimesRepository;

/**
 * ClassService 테스트
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("클래스 등록 서비스 테스트")
class ClassServiceTest {

	@Mock
	private ClassesRepository classesRepository;

	@Mock
	private CategoriesRepository categoriesRepository;

	@Mock
	private TimesRepository timesRepository;

	@Mock
	private ImageRepository imageRepository;

	@InjectMocks
	private ClassService classService;

	private User teacher;
	private Categories category;
	private Classes classes;
	private RegisterClassRequest request;

	@BeforeEach
	void setUp() {
		// 강사 생성
		teacher = User.builder()
				.email("teacher@example.com")
				.password("encodedPassword")
				.name("홍길동")
				.role(Role.USER)
				.build();
		// Reflection을 사용하여 ID 설정 (JPA 저장 없이 테스트를 위해)
		org.springframework.test.util.ReflectionTestUtils.setField(teacher, "id", 1L);

		// 카테고리 생성 (요리/베이킹)
		category = Categories.builder()
				.categoryId(1)
				.category(CategoryType.COOKING_BAKING.getKoreanName())
				.build();

		// 클래스 생성
		classes = Classes.builder()
				.classId(1)
				.teacher(teacher)
				.category(category)
				.className("홈 베이킹 클래스")
				.classDetail("집에서 쉽게 만드는 빵과 케이크")
				.location("서울시 강남구")
				.maxCapacity(10)
				.price(50000)
				.build();

		// 요청 DTO 생성
		request = new RegisterClassRequest(
				CategoryType.COOKING_BAKING,
				"홈 베이킹 클래스",
				"집에서 쉽게 만드는 빵과 케이크",
				"1회차: 식빵, 2회차: 케이크",
				"재료비, 도구 대여",
				"앞치마",
				"서울시 강남구",
				"127.0276",
				"37.4979",
				"06234",
				10,
				50000,
				List.of(
						LocalDate.of(2025, 2, 1),
						LocalDate.of(2025, 2, 3),
						LocalDate.of(2025, 2, 5)
				),
				LocalTime.of(14, 0),
				LocalTime.of(16, 0),
				List.of(
						new ImageDto("/uploads/img1.jpg"),
						new ImageDto("/uploads/img2.jpg")
				)
		);
	}

	@Test
	@DisplayName("클래스 등록 - 성공")
	void registerClass_Success() {
		// given
		when(categoriesRepository.findByCategory(CategoryType.COOKING_BAKING.getKoreanName()))
				.thenReturn(Optional.of(category));
		when(timesRepository.existsOverlappingTimesByTeacher(anyLong(), any(), any())).thenReturn(false);
		when(classesRepository.save(any(Classes.class))).thenReturn(classes);
		when(timesRepository.saveAll(anyList())).thenReturn(List.of());
		when(imageRepository.saveAll(anyList())).thenReturn(List.of());

		// when
		RegisterClassResponse response = classService.registerClass(teacher, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.classId()).isEqualTo(1);
		assertThat(response.className()).isEqualTo("홈 베이킹 클래스");
		assertThat(response.category()).isEqualTo(CategoryType.COOKING_BAKING.getKoreanName());

		// 검증
		verify(categoriesRepository, times(1)).findByCategory(CategoryType.COOKING_BAKING.getKoreanName());
		verify(timesRepository, times(3)).existsOverlappingTimesByTeacher(anyLong(), any(), any());
		verify(classesRepository, times(1)).save(any(Classes.class));
		verify(timesRepository, times(1)).saveAll(anyList());
		verify(imageRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("클래스 등록 - 실패 (존재하지 않는 카테고리)")
	void registerClass_CategoryNotFound() {
		// given
		when(categoriesRepository.findByCategory(CategoryType.COOKING_BAKING.getKoreanName()))
				.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> classService.registerClass(teacher, request))
				.isInstanceOf(CategoryNotFoundException.class)
				.hasMessageContaining("존재하지 않는 카테고리입니다");

		// 검증
		verify(categoriesRepository, times(1)).findByCategory(CategoryType.COOKING_BAKING.getKoreanName());
		verify(classesRepository, never()).save(any(Classes.class));
	}

	@Test
	@DisplayName("클래스 등록 - 실패 (유효하지 않은 시간)")
	void registerClass_InvalidTime() {
		// given
		RegisterClassRequest invalidRequest = new RegisterClassRequest(
				CategoryType.COOKING_BAKING, "홈 베이킹 클래스", "상세 설명", null, null, null,
				"서울시 강남구", null, null, null, 10, 50000,
				List.of(LocalDate.of(2025, 2, 1)),
				LocalTime.of(16, 0),  // 시작 시간
				LocalTime.of(14, 0),  // 종료 시간 (시작보다 이른 시간)
				List.of(new ImageDto("/uploads/img1.jpg"))
		);

		when(categoriesRepository.findByCategory(CategoryType.COOKING_BAKING.getKoreanName()))
				.thenReturn(Optional.of(category));

		// when & then
		assertThatThrownBy(() -> classService.registerClass(teacher, invalidRequest))
				.isInstanceOf(InvalidClassTimeException.class)
				.hasMessage("시작 시간은 종료 시간보다 이전이어야 합니다");

		// 검증
		verify(categoriesRepository, times(1)).findByCategory(CategoryType.COOKING_BAKING.getKoreanName());
		verify(classesRepository, never()).save(any(Classes.class));
	}

	@Test
	@DisplayName("클래스 등록 - 실패 (시간 중복)")
	void registerClass_DuplicateTime() {
		// given
		when(categoriesRepository.findByCategory(CategoryType.COOKING_BAKING.getKoreanName()))
				.thenReturn(Optional.of(category));
		when(timesRepository.existsOverlappingTimesByTeacher(anyLong(), any(), any())).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> classService.registerClass(teacher, request))
				.isInstanceOf(DuplicateClassTimeException.class)
				.hasMessageContaining("이미 등록된 시간대입니다");

		// 검증
		verify(categoriesRepository, times(1)).findByCategory(CategoryType.COOKING_BAKING.getKoreanName());
		verify(timesRepository, times(1)).existsOverlappingTimesByTeacher(anyLong(), any(), any());
		verify(classesRepository, never()).save(any(Classes.class));
	}

	@Test
	@DisplayName("클래스 등록 - 실패 (이미지 없음)")
	void registerClass_NoImages() {
		// given
		RegisterClassRequest invalidRequest = new RegisterClassRequest(
				CategoryType.COOKING_BAKING, "홈 베이킹 클래스", "상세 설명", null, null, null,
				"서울시 강남구", null, null, null, 10, 50000,
				List.of(LocalDate.of(2025, 2, 1)),
				LocalTime.of(14, 0),
				LocalTime.of(16, 0),
				List.of()  // 이미지 없음
		);

		when(categoriesRepository.findByCategory(CategoryType.COOKING_BAKING.getKoreanName()))
				.thenReturn(Optional.of(category));
		when(timesRepository.existsOverlappingTimesByTeacher(anyLong(), any(), any())).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> classService.registerClass(teacher, invalidRequest))
				.isInstanceOf(InvalidImageException.class)
				.hasMessage("이미지는 1개 이상 5개 이하로 등록해야 합니다");

		// 검증
		verify(classesRepository, never()).save(any(Classes.class));
	}

	@Test
	@DisplayName("클래스 등록 - 실패 (이미지 6개 초과)")
	void registerClass_TooManyImages() {
		// given
		RegisterClassRequest invalidRequest = new RegisterClassRequest(
				CategoryType.COOKING_BAKING, "홈 베이킹 클래스", "상세 설명", null, null, null,
				"서울시 강남구", null, null, null, 10, 50000,
				List.of(LocalDate.of(2025, 2, 1)),
				LocalTime.of(14, 0),
				LocalTime.of(16, 0),
				List.of(
						new ImageDto("/img1.jpg"),
						new ImageDto("/img2.jpg"),
						new ImageDto("/img3.jpg"),
						new ImageDto("/img4.jpg"),
						new ImageDto("/img5.jpg"),
						new ImageDto("/img6.jpg")  // 6개 (초과)
				)
		);

		when(categoriesRepository.findByCategory(CategoryType.COOKING_BAKING.getKoreanName()))
				.thenReturn(Optional.of(category));
		when(timesRepository.existsOverlappingTimesByTeacher(anyLong(), any(), any())).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> classService.registerClass(teacher, invalidRequest))
				.isInstanceOf(InvalidImageException.class)
				.hasMessage("이미지는 1개 이상 5개 이하로 등록해야 합니다");

		// 검증
		verify(classesRepository, never()).save(any(Classes.class));
	}

	@Test
	@DisplayName("클래스 등록 - 다중 날짜 등록 확인")
	void registerClass_MultipleDates() {
		// given
		when(categoriesRepository.findByCategory(CategoryType.COOKING_BAKING.getKoreanName()))
				.thenReturn(Optional.of(category));
		when(timesRepository.existsOverlappingTimesByTeacher(anyLong(), any(), any())).thenReturn(false);
		when(classesRepository.save(any(Classes.class))).thenReturn(classes);
		when(timesRepository.saveAll(anyList())).thenAnswer(invocation -> {
			List<com.oneday.core.entity.Times> times = invocation.getArgument(0);
			assertThat(times).hasSize(3);  // 3개 날짜
			return times;
		});
		when(imageRepository.saveAll(anyList())).thenReturn(List.of());

		// when
		RegisterClassResponse response = classService.registerClass(teacher, request);

		// then
		assertThat(response).isNotNull();

		// 3개 날짜에 대해 중복 체크 3번 호출 확인
		verify(timesRepository, times(3)).existsOverlappingTimesByTeacher(
				eq(teacher.getId()),
				any(LocalDateTime.class),
				any(LocalDateTime.class)
		);

		// Times 저장 시 3개 생성 확인
		verify(timesRepository, times(1)).saveAll(argThat(list ->
				((List<?>) list).size() == 3
		));
	}
}
