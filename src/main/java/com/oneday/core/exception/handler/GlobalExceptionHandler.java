package com.oneday.core.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.exception.user.DuplicateEmailException;
import com.oneday.core.exception.user.UserNotFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리 핸들러
 * 애플리케이션 전역에서 발생하는 예외를 처리합니다.
 *
 * @author zionge2k
 * @since 2025-10-31
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 유효성 검증 실패 예외 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e
	) {
		FieldError fieldError = e.getBindingResult().getFieldError();
		String message = fieldError != null ? fieldError.getDefaultMessage() : "잘못된 입력값입니다";

		log.warn("유효성 검증 실패: {}", message);

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.error("INVALID_INPUT_VALUE", message));
	}

	/**
	 * 이메일 중복 예외 처리
	 */
	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ApiResponse<Void>> handleDuplicateEmailException(
		DuplicateEmailException e
	) {
		log.warn("이메일 중복: {}", e.getMessage());

		return ResponseEntity
			.status(HttpStatus.CONFLICT)
			.body(ApiResponse.error("U002", e.getMessage()));
	}

	/**
	 * 사용자를 찾을 수 없음 예외 처리
	 */
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(
		UserNotFoundException e
	) {
		log.warn("사용자를 찾을 수 없음: {}", e.getMessage());

		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.body(ApiResponse.error("U001", e.getMessage()));
	}

	/**
	 * 기타 예외 처리
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		log.error("서버 내부 오류 발생", e);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponse.error("C004", "서버 내부 오류가 발생했습니다"));
	}
}

