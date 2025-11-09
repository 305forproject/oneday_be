package com.oneday.core.dto.common;

import com.oneday.core.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 공통 응답 포맷
 *
 * @param <T> 응답 데이터 타입
 * @author zionge2k
 * @since 2025-01-26
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

	private final boolean success;
	private final T data;
	private final ErrorResponse error;

	/**
	 * 성공 응답 생성
	 *
	 * @param data 응답 데이터
	 * @param <T> 데이터 타입
	 * @return 성공 응답
	 */
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	/**
	 * 성공 응답 생성 (데이터 없음)
	 *
	 * @param <T> 데이터 타입
	 * @return 성공 응답
	 */
	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>(true, null, null);
	}

	/**
	 * 실패 응답 생성
	 *
	 * @param errorCode 에러 코드
	 * @param <T> 데이터 타입
	 * @return 실패 응답
	 */
	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		return new ApiResponse<>(
				false,
				null,
				new ErrorResponse(errorCode.getCode(), errorCode.getMessage())
		);
	}

	/**
	 * 실패 응답 생성 (커스텀 메시지)
	 *
	 * @param errorCode 에러 코드
	 * @param message 커스텀 메시지
	 * @param <T> 데이터 타입
	 * @return 실패 응답
	 */
	public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
		return new ApiResponse<>(
				false,
				null,
				new ErrorResponse(errorCode.getCode(), message)
		);
	}

	/**
	 * 실패 응답 생성 (상세 데이터 포함)
	 *
	 * @param errorCode 에러 코드
	 * @param data 에러 상세 데이터
	 * @param <T> 데이터 타입
	 * @return 실패 응답
	 */
	public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
		return new ApiResponse<>(
				false,
				data,
				new ErrorResponse(errorCode.getCode(), errorCode.getMessage())
		);
	}

	/**
	 * 실패 응답 생성 (ErrorResponse 직접 전달)
	 *
	 * @param errorResponse 에러 응답
	 * @param <T> 데이터 타입
	 * @return 실패 응답
	 */
	public static <T> ApiResponse<T> error(ErrorResponse errorResponse) {
		return new ApiResponse<>(false, null, errorResponse);
	}

	/**
	 * 에러 응답 정보
	 */
	public record ErrorResponse(String code, String message) {
	}
}

