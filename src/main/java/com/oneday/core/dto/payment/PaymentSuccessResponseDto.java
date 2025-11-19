package com.oneday.core.dto.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.oneday.core.entity.Payment;

@Getter
@NoArgsConstructor
public class PaymentSuccessResponseDto {

	private int paymentId;
	private int reservationId;
	private String tossOrderId;
	private String tossPaymentKey;
	private String tossPaymentMethod;
	private String tossPaymentStatus;
	private LocalDateTime requestedAt;
	private LocalDateTime approvedAt;
	private int totalAmount;

	// 엔티티 -> DTO 변환 생성자
	public PaymentSuccessResponseDto(Payment payment) {
		this.paymentId = payment.getPaymentId();

		// Lazy Loading 문제 해결: 객체를 직렬화하지 않고 ID만 꺼냄
		if (payment.getReservation() != null) {
			this.reservationId = payment.getReservation().getTime().getTimeId();
		}

		this.tossOrderId = payment.getTossOrderId();
		this.tossPaymentKey = payment.getTossPaymentKey();
		this.tossPaymentMethod = payment.getTossPaymentMethod();
		this.tossPaymentStatus = payment.getTossPaymentStatus();
		this.requestedAt = payment.getRequestedAt();
		this.approvedAt = payment.getApprovedAt();
		this.totalAmount = payment.getTotalAmount();
	}
}
