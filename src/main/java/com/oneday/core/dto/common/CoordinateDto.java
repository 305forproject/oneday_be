package com.oneday.core.dto.common;

/**
 * 좌표 정보 DTO
 * <p>
 * Kakao Map API에서 반환된 위도/경도 정보를 담습니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-27
 */
public record CoordinateDto(
		String latitude,
		String longitude
) {
}
