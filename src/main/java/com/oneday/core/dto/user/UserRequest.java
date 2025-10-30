package com.oneday.core.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 사용자 요청 DTO
 *
 * @author Zion
 * @since 2025-10-30
 */
@Schema(description = "사용자 요청")
public record UserRequest(
    @Schema(
        description = "이메일 주소",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @Schema(
        description = "비밀번호",
        example = "password123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호는 필수입니다.")
    String password,

    @Schema(
        description = "사용자 이름",
        example = "홍길동",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이름은 필수입니다.")
    String name
) {
}

