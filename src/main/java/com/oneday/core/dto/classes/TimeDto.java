package com.oneday.core.dto.classes;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * 클래스 시간 정보 DTO
 * <p>
 * 클래스 등록 시 날짜 정보를 전달하는 내부 DTO입니다.
 * 다중 날짜 선택을 위해 LocalDate만 포함합니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public record TimeDto(
		@NotNull(message = "날짜는 필수입니다")
		LocalDate date
) {
}
