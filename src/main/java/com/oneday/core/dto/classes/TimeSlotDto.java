package com.oneday.core.dto.classes;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

/**
 * 클래스 등록용 시간대 DTO
 * <p>
 * 클래스 등록 시 날짜별 시간 정보를 전달합니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public record TimeSlotDto(
		@NotNull(message = "날짜는 필수입니다")
		LocalDate date,

		@NotNull(message = "시작 시간은 필수입니다")
		LocalTime startTime,

		@NotNull(message = "종료 시간은 필수입니다")
		LocalTime endTime
) {
}
