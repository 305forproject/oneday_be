package com.oneday.core.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO
 *
 * @author zionge2k
 * @since 2025-10-31
 */
@Schema(description = "회원가입 요청")
public record SignUpRequest(

	@Schema(
		description = "이메일 주소",
		example = "user@example.com",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	@NotBlank(message = "이메일은 필수입니다")
	@Email(message = "이메일 형식이 올바르지 않습니다")
	String email,

	@Schema(
		description = "비밀번호 (8자 이상)",
		example = "password123!",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	@NotBlank(message = "비밀번호는 필수입니다")
	@Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
	String password,

	@Schema(
		description = "사용자 이름",
		example = "홍길동",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	@NotBlank(message = "이름은 필수입니다")
	String name
) {
}

