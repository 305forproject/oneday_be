package com.oneday.core.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 권한 열거형
 *
 * @author Zion
 * @since 2025-10-30
 */
@Getter
@RequiredArgsConstructor
public enum Role {

    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String description;
}

