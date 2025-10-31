package com.oneday.core.dto.auth;

import java.time.LocalDateTime;

import com.oneday.core.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 회원가입 응답 DTO
 *
 * @author zionge2k
 * @since 2025-10-31
 */
@Schema(description = "회원가입 응답")
public record SignUpResponse(

  @Schema(description = "사용자 ID", example = "1")
  Long id,

  @Schema(description = "이메일 주소", example = "user@example.com")
  String email,

  @Schema(description = "사용자 이름", example = "홍길동")
  String name,

  @Schema(
    description = "가입 일시",
    example = "2025-10-31T10:00:00",
    type = "string",
    format = "date-time"
  )
  LocalDateTime createdAt
) {
  public static SignUpResponse from(User user) {
    return new SignUpResponse(
      user.getId(),
      user.getEmail(),
      user.getName(),
      user.getCreatedAt()
    );
  }
}

