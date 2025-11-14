package com.oneday.core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.oneday.core.dto.payment.PaymentRequestDto;
import com.oneday.core.entity.Payment;
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
	 */
	@PostMapping("/complete")
	public ResponseEntity<?> completePayment(
			@RequestBody PaymentRequestDto paymentDto,
			HttpSession session) {

		if (session == null || session.getAttribute("userId") == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다."));
		}

		long studentId = (Long)session.getAttribute("userId");

		try {
			Payment completedPayment = paymentService.createReservationAndPayment(
					paymentDto.getTimeId(),
					studentId,
					paymentDto.getTossResponse()
			);

			// 저장된 결제 정보 반환
			return ResponseEntity.status(HttpStatus.CREATED).body(completedPayment);

		} catch (RuntimeException e) {
			log.error("결제 및 예약 생성 실패: {}", e.getMessage(), e);
			return ResponseEntity.badRequest()
					.body(Map.of("message", e.getMessage()));
		}
	}
}
