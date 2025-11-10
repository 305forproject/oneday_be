package com.oneday.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.oneday.core.entity.Payment;
import com.oneday.core.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final ReservationService reservationService;
	// 예약 확정 상태 ID
	private static final int CONFIRMED_STATUS_ID = 1;

	/**
	 * 결제 승인 후, 예약 생성과 결제 정보 저장을 동시 처리
	 */
	@Transactional
	public Payment createReservationAndPayment(int classId, int studentId, Map<String, Object> tossResponse) {

		Reservation savedReservation = reservationService.createReservation(classId, studentId);
		// 결제 정보 저장
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

		Payment newPayment = Payment.builder()
			.reservation(savedReservation)
			.tossOrderId((String) tossResponse.get("orderId"))
			.tossPaymentKey((String) tossResponse.get("paymentKey"))
			.tossPaymentMethod((String) tossResponse.get("method"))
			.tossPaymentStatus((String) tossResponse.get("status"))
			.totalAmount(((Number) tossResponse.get("totalAmount")).intValue())
			.requestedAt(OffsetDateTime.parse((String) tossResponse.get("requestedAt"), formatter).toLocalDateTime())
			.approvedAt(OffsetDateTime.parse((String) tossResponse.get("approvedAt"), formatter).toLocalDateTime())
			.build();

		return paymentRepository.save(newPayment);
	}
}