package com.oneday.core.dto.classes;

import jakarta.validation.constraints.NotBlank;

/**
 * 이미지 정보 DTO
 * <p>
 * 클래스 등록 시 이미지 URL을 전달하는 내부 DTO입니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public record ImageDto(
		@NotBlank(message = "이미지 URL은 필수입니다")
		String imageUrl
) {
}
