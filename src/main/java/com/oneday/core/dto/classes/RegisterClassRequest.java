package com.oneday.core.dto.classes;

import java.util.List;

import com.oneday.core.entity.CategoryType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 클래스 등록 요청 DTO
 * <p>
 * 클래스 등록에 필요한 모든 정보를 전달합니다.
 * 날짜별 시간 정보를 schedules 리스트로 전달합니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public record RegisterClassRequest(
		@NotNull(message = "카테고리는 필수입니다")
		CategoryType category,

		@NotBlank(message = "클래스명은 필수입니다")
		@Size(max = 50, message = "클래스명은 50자 이내여야 합니다")
		String className,

		@Size(max = 255, message = "클래스 상세는 255자 이내여야 합니다")
		String classDetail,

		@Size(max = 255, message = "커리큘럼은 255자 이내여야 합니다")
		String curriculum,

		@Size(max = 255, message = "포함 사항은 255자 이내여야 합니다")
		String included,

		@Size(max = 255, message = "준비물은 255자 이내여야 합니다")
		String required,

		@NotBlank(message = "위치는 필수입니다")
		@Size(max = 255, message = "위치는 255자 이내여야 합니다")
		String location,

		@Size(max = 20, message = "경도는 20자 이내여야 합니다")
		String longitude,

		@Size(max = 20, message = "위도는 20자 이내여야 합니다")
		String latitude,

		String zipcode,

		@NotNull(message = "최대 인원은 필수입니다")
		@Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다")
		Integer maxCapacity,

		@NotNull(message = "가격은 필수입니다")
		@Min(value = 0, message = "가격은 0원 이상이어야 합니다")
		Integer price,

		@NotNull(message = "일정 정보는 필수입니다")
		@Size(min = 1, message = "최소 1개 이상의 일정을 추가해야 합니다")
		@Valid
		List<TimeSlotDto> schedules
) {
}
