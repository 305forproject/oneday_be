package com.oneday.core.dto.auth;

/**
 * 로그인 응답 DTO
 *
 * @param accessToken JWT Access Token
 * @param refreshToken JWT Refresh Token
 * @author zionge2k
 * @since 2025-01-26
 */
public record LoginResponse(
    String accessToken,
    String refreshToken
) {
}

