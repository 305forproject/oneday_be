package com.oneday.core.dto.classes;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.oneday.core.entity.CategoryType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * RegisterClassRequest Validation 테스트
 * <p>
 * Jakarta Validation 어노테이션의 동작을 검증합니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@DisplayName("RegisterClassRequest Validation 테스트")
class RegisterClassRequestValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	/**
	 * 유효한 요청 생성 헬퍼 메서드
	 */
	private RegisterClassRequest createValidRequest() {
		return new RegisterClassRequest(
				CategoryType.COOKING_BAKING,
				"홈 베이킹 클래스",
				"초보자를 위한 베이킹",
				"1회차: 식빵 만들기",
				"재료비, 도구 대여",
				"앞치마, 필기구",
				"서울시 강남구 테헤란로 123",
				"127.0276",
				"37.4979",
				"06234",
				10,
				50000,
				List.of(LocalDate.of(2025, 2, 1)),
				LocalTime.of(14, 0),
				LocalTime.of(16, 0),
				List.of(new ImageDto("/uploads/class1_img1.jpg"))
		);
	}

	@Test
	@DisplayName("유효한 요청 - 검증 통과")
	void validRequest_NoViolations() {
		// given
		RegisterClassRequest request = createValidRequest();

		// when
		Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isEmpty();
	}

	@Nested
	@DisplayName("카테고리 검증")
	class CategoryValidation {

		@Test
		@DisplayName("카테고리 null - 검증 실패")
		void nullCategory_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					null,  // 카테고리 null
					"홈 베이킹 클래스",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("카테고리는 필수입니다");
		}
	}

	@Nested
	@DisplayName("클래스명 검증")
	class ClassNameValidation {

		@Test
		@DisplayName("빈 클래스명 - 검증 실패")
		void blankClassName_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"",  // 빈 값
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("클래스명은 필수입니다");
		}

		@Test
		@DisplayName("클래스명 null - 검증 실패")
		void nullClassName_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					null,  // null
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("클래스명은 필수입니다");
		}

		@Test
		@DisplayName("클래스명 50자 초과 - 검증 실패")
		void classNameTooLong_ViolationOccurs() {
			// given
			String longName = "a".repeat(51);  // 51자
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					longName,
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("클래스명은 50자 이내여야 합니다");
		}

		@Test
		@DisplayName("클래스명 정확히 50자 - 검증 통과")
		void classNameExactly50Chars_NoViolation() {
			// given
			String name = "a".repeat(50);  // 정확히 50자
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					name,
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isEmpty();
		}
	}

	@Nested
	@DisplayName("위치 검증")
	class LocationValidation {

		@Test
		@DisplayName("빈 위치 - 검증 실패")
		void blankLocation_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"",  // 빈 값
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("위치는 필수입니다");
		}

		@Test
		@DisplayName("위치 255자 초과 - 검증 실패")
		void locationTooLong_ViolationOccurs() {
			// given
			String longLocation = "a".repeat(256);  // 256자
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					longLocation,
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("위치는 255자 이내여야 합니다");
		}
	}

	@Nested
	@DisplayName("최대 인원 검증")
	class MaxCapacityValidation {

		@Test
		@DisplayName("최대 인원 null - 검증 실패")
		void nullMaxCapacity_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					null,  // null
					50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("최대 인원은 필수입니다");
		}

		@Test
		@DisplayName("최대 인원 0 - 검증 실패")
		void maxCapacityZero_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					0,  // 0
					50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("최대 인원은 1명 이상이어야 합니다");
		}

		@Test
		@DisplayName("최대 인원 음수 - 검증 실패")
		void maxCapacityNegative_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					-1,  // 음수
					50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("최대 인원은 1명 이상이어야 합니다");
		}

		@Test
		@DisplayName("최대 인원 1 - 검증 통과")
		void maxCapacityOne_NoViolation() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					1,  // 1
					50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isEmpty();
		}
	}

	@Nested
	@DisplayName("가격 검증")
	class PriceValidation {

		@Test
		@DisplayName("가격 null - 검증 실패")
		void nullPrice_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10,
					null,  // null
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("가격은 필수입니다");
		}

		@Test
		@DisplayName("가격 음수 - 검증 실패")
		void negativePrice_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10,
					-1,  // 음수
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("가격은 0원 이상이어야 합니다");
		}

		@Test
		@DisplayName("가격 0 - 검증 통과 (무료 클래스)")
		void priceZero_NoViolation() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10,
					0,  // 0원
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isEmpty();
		}
	}

	@Nested
	@DisplayName("날짜 검증")
	class DatesValidation {

		@Test
		@DisplayName("날짜 null - 검증 실패")
		void nullDates_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					null,  // null
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("날짜 정보는 필수입니다");
		}

		@Test
		@DisplayName("날짜 빈 리스트 - 검증 실패")
		void emptyDates_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					new ArrayList<>(),  // 빈 리스트
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("최소 1개 이상의 날짜를 선택해야 합니다");
		}

		@Test
		@DisplayName("날짜 1개 - 검증 통과")
		void oneDateSufficient_NoViolation() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),  // 1개
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isEmpty();
		}

		@Test
		@DisplayName("날짜 5개 - 검증 통과")
		void multipleDates_NoViolation() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(
							LocalDate.of(2025, 2, 1),
							LocalDate.of(2025, 2, 3),
							LocalDate.of(2025, 2, 5),
							LocalDate.of(2025, 2, 7),
							LocalDate.of(2025, 2, 9)
					),  // 5개
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isEmpty();
		}
	}

	@Nested
	@DisplayName("시간 검증")
	class TimeValidation {

		@Test
		@DisplayName("시작 시간 null - 검증 실패")
		void nullStartTime_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					null,  // null
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("시작 시간은 필수입니다");
		}

		@Test
		@DisplayName("종료 시간 null - 검증 실패")
		void nullEndTime_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					null,  // null
					List.of(new ImageDto("/img1.jpg"))
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("종료 시간은 필수입니다");
		}
	}

	@Nested
	@DisplayName("이미지 검증")
	class ImagesValidation {

		@Test
		@DisplayName("이미지 null - 검증 실패")
		void nullImages_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					null  // null
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("이미지 정보는 필수입니다");
		}

		@Test
		@DisplayName("이미지 빈 리스트 - 검증 실패")
		void emptyImages_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					new ArrayList<>()  // 빈 리스트
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("이미지는 1개 이상 5개 이하로 등록해야 합니다");
		}

		@Test
		@DisplayName("이미지 6개 - 검증 실패")
		void tooManyImages_ViolationOccurs() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(
							new ImageDto("/img1.jpg"),
							new ImageDto("/img2.jpg"),
							new ImageDto("/img3.jpg"),
							new ImageDto("/img4.jpg"),
							new ImageDto("/img5.jpg"),
							new ImageDto("/img6.jpg")  // 6개
					)
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isNotEmpty();
			assertThat(violations)
					.extracting(ConstraintViolation::getMessage)
					.contains("이미지는 1개 이상 5개 이하로 등록해야 합니다");
		}

		@Test
		@DisplayName("이미지 1개 - 검증 통과")
		void oneImage_NoViolation() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(new ImageDto("/img1.jpg"))  // 1개
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isEmpty();
		}

		@Test
		@DisplayName("이미지 5개 - 검증 통과")
		void fiveImages_NoViolation() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					CategoryType.COOKING_BAKING,
					"클래스명",
					"상세 설명",
					null, null, null,
					"서울시 강남구",
					null, null, null,
					10, 50000,
					List.of(LocalDate.of(2025, 2, 1)),
					LocalTime.of(14, 0),
					LocalTime.of(16, 0),
					List.of(
							new ImageDto("/img1.jpg"),
							new ImageDto("/img2.jpg"),
							new ImageDto("/img3.jpg"),
							new ImageDto("/img4.jpg"),
							new ImageDto("/img5.jpg")  // 5개
					)
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).isEmpty();
		}
	}

	@Nested
	@DisplayName("복합 검증")
	class MultipleValidation {

		@Test
		@DisplayName("여러 필드 동시 검증 실패")
		void multipleViolations() {
			// given
			RegisterClassRequest request = new RegisterClassRequest(
					null,  // 카테고리 null
					"",    // 클래스명 빈 값
					"상세 설명",
					null, null, null,
					"",    // 위치 빈 값
					null, null, null,
					0,     // 최대 인원 0
					-1,    // 가격 음수
					new ArrayList<>(),  // 날짜 빈 리스트
					null,  // 시작 시간 null
					null,  // 종료 시간 null
					new ArrayList<>()   // 이미지 빈 리스트
			);

			// when
			Set<ConstraintViolation<RegisterClassRequest>> violations = validator.validate(request);

			// then
			assertThat(violations).hasSize(9);  // 9개 검증 실패
		}
	}
}
