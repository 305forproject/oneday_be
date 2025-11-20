package com.oneday.core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.oneday.core.config.security.UserPrincipal;
import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.dto.payment.PaymentRequestDto;
import com.oneday.core.dto.payment.PaymentSuccessResponseDto;
import com.oneday.core.entity.Payment;
import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;
import com.oneday.core.service.PaymentService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	/**
	 * 클라이언트에서 토스 결제 승인이 완료된 후,
	 * 최종 예약 및 결제 정보 저장을 요청하는 엔드포인트
	 * @param principal 인증된 사용자 정보
	 * @param paymentDto 결제 요청 정보
	 * @return 결제 및 예약 정보
	 */
	@PostMapping("/complete")
	public ResponseEntity<ApiResponse<PaymentSuccessResponseDto>> completePayment(
			@RequestBody PaymentRequestDto paymentDto,
			@AuthenticationPrincipal UserPrincipal principal) {

		// 1. 로그인 체크
		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
		}

		long studentId = principal.getId();
		log.info("결제 요청 유저 ID: {}", studentId);

		try {
			// 1. 서비스 호출 (저장까지는 이미 잘 되고 있음)
			Payment completedPayment = paymentService.processPayment(studentId, paymentDto);

			// 2. 여기서 DTO로 변환 (Lazy Loading 프록시 문제 해결)
			PaymentSuccessResponseDto responseDto = new PaymentSuccessResponseDto(completedPayment);

			// 3. DTO 반환
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(responseDto));

		} catch (CustomException e) {
			log.warn("비즈니스 로직 예외 발생: {}", e.getErrorCode().getMessage());

			// 409 Conflict (중복/충돌) 또는 400 Bad Request 사용
			return ResponseEntity
					.status(e.getErrorCode().getStatus())
					.body(ApiResponse.error(e.getErrorCode()));

		} catch (Exception e) {
			log.error("결제 처리 중 알 수 없는 오류 발생: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}
}
