package com.oneday.core.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 사용자 요청 DTO
 *
 * @author Zion
 * @since 2025-10-30
 */
public record UserRequest(
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    String password,

    @NotBlank(message = "이름은 필수입니다.")
    String name
) {
}

