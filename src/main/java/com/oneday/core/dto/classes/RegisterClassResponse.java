package com.oneday.core.dto.classes;

/**
 * 클래스 등록 응답 DTO
 * <p>
 * 클래스 등록 완료 후 반환하는 정보입니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public record RegisterClassResponse(
		Integer classId,
		String className,
		String category
) {
}
