package com.oneday.core.dto.payment;

import lombok.Getter;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {
	private int timeId;        // 예약할 시간 ID
	private String paymentKey; // 토스 결제 키
	private String orderId;    // 주문 ID
	private long amount;       // 결제 금액
}
