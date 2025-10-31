package com.oneday.core.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API 공통 응답 DTO
 *
 * @author zionge2k
 * @since 2025-10-31
 * @param <T> 응답 데이터 타입
 */
@Schema(description = "API 공통 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
  @Schema(description = "성공 여부", example = "true")
  boolean success,

  @Schema(description = "응답 데이터")
  T data,

  @Schema(description = "에러 정보")
  ErrorInfo error
) {
  /**
   * 성공 응답 생성
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, data, null);
  }

  /**
   * 실패 응답 생성
   */
  public static <T> ApiResponse<T> error(String code, String message) {
    return new ApiResponse<>(false, null, new ErrorInfo(code, message));
  }

  /**
   * 에러 정보
   */
  @Schema(description = "에러 정보")
  public record ErrorInfo(
    @Schema(description = "에러 코드", example = "U002")
    String code,

    @Schema(description = "에러 메시지", example = "이미 사용 중인 이메일입니다")
    String message
  ) {
  }
}

