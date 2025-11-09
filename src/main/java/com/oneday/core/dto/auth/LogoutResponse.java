package com.oneday.core.dto.auth;

import java.time.LocalDateTime;

/**
 * 로그아웃 응답 DTO
 */
public record LogoutResponse(
		String message,
		LocalDateTime logoutAt
) {
	public LogoutResponse(String message) {
		this(message, LocalDateTime.now());
	}

	public static LogoutResponse success() {
		return new LogoutResponse("로그아웃되었습니다");
	}
}

