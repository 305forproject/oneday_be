package com.oneday.core.dto.auth;

import java.time.LocalDateTime;

public record SignUpResponse(
    Long id,
    String email,
    String name,
    LocalDateTime createdAt
) {
}

