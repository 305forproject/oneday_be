package com.oneday.core.dto.user;

import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 *
 * @author Zion
 * @since 2025-10-30
 */
@Schema(description = "사용자 응답")
public record UserResponse(
    @Schema(description = "사용자 ID", example = "1")
    Long id,

    @Schema(description = "이메일 주소", example = "user@example.com")
    String email,

    @Schema(description = "사용자 이름", example = "홍길동")
    String name,

    @Schema(description = "사용자 권한", example = "USER")
    Role role,

    @Schema(
        description = "생성 일시",
        example = "2025-01-30T10:00:00",
        type = "string",
        format = "date-time"
    )
    LocalDateTime createdAt,

    @Schema(
        description = "수정 일시",
        example = "2025-01-30T10:00:00",
        type = "string",
        format = "date-time"
    )
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

