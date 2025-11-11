package com.oneday.core.dto.auth;

public record TokenRefreshResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn
) {
    public TokenRefreshResponse(String accessToken, String refreshToken, Long expiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn);
    }
}

