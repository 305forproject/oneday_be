package com.oneday.core.dto.auth;

/**
 * 로그인 응답 DTO
 *
 * @param accessToken 액세스 토큰
 * @param refreshToken 리프레시 토큰
 * @param email 이메일
 * @param name 이름
 * @author Zion
 * @since 2025-10-31
 */
public record LoginResponse(
  String accessToken,
  String refreshToken,
  String email,
  String name
) {
}

