package com.oneday.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_id")
	private int paymentId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id")
	private Reservation reservation;

	@Column(name = "toss_order_id", unique = true)
	private String tossOrderId;

	@Column(name = "toss_payment_key")
	private String tossPaymentKey;

	@Column(name = "toss_payment_method")
	private String tossPaymentMethod;

	@Column(name = "toss_payment_status")
	private String tossPaymentStatus;

	@Column(name = "requested_at")
	private LocalDateTime requestedAt;

	@Column(name = "approved_at")
	private LocalDateTime approvedAt;

	@Column(name = "total_amount")
	private int totalAmount;
}