package com.oneday.core.dto.auth;

/**
 * 로그인 응답 DTO
 *
 * @param accessToken JWT Access Token
 * @param refreshToken JWT Refresh Token
 * @param userId 사용자 ID
 * @param email 사용자 이메일
 * @param name 사용자 이름
 * @author zionge2k
 * @since 2025-01-26
 */
public record LoginResponse(
    String accessToken,
    String refreshToken,
    Long userId,
    String email,
    String name
) {
}

