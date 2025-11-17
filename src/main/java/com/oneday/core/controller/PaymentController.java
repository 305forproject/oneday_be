package com.oneday.core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.dto.payment.PaymentRequestDto;
import com.oneday.core.entity.Payment;
import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;
import com.oneday.core.service.PaymentService;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	/**
	 * 클라이언트에서 토스 결제 승인이 완료된 후,
	 * 최종 예약 및 결제 정보 저장을 요청하는 엔드포인트
	 * @param authenticationprincipal 인증된 사용자 정보
	 * @param PaymentRequestDto 결제 요청 정보
	 * @return 결제 및 예약 정보
	 */
	@PostMapping("/complete")
	public ResponseEntity<ApiResponse<Payment>> completePayment(
			@RequestBody PaymentRequestDto paymentDto,
			@AuthenticationPrincipal Long studentId) {

		if (studentId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
		}

		log.info("인증된 사용자 ID: {}", studentId);

		try {
			Payment completedPayment = paymentService.createReservationAndPayment(
					paymentDto.getTimeId(),
					studentId,
					paymentDto.getTossResponse()
			);

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(completedPayment));

		} catch (CustomException e) {
			log.warn("결제 및 예약 생성 실패: {}", e.getMessage());
			return ResponseEntity.status(e.getErrorCode().getStatus())
					.body(ApiResponse.error(e.getErrorCode()));
		} catch (Exception e) {
			log.error("결제 및 예약 생성 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}
}
