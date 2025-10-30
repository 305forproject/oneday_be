package com.oneday.core.dto.user;

import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 *
 * @author Zion
 * @since 2025-10-30
 */
public record UserResponse(
    Long id,
    String email,
    String name,
    Role role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * Entity로부터 Response DTO 생성 (정적 팩토리 메서드)
     */
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}

