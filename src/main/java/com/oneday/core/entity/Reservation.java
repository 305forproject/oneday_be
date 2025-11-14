package com.oneday.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 예약 정보 엔티티
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Entity
@Table(name = "reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@ToString(exclude = {"time", "user", "status"})
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reservation_id")
	private Integer reservationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "time_id", nullable = false)
	private Times time;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "status_code", nullable = false)
	private ReservationStatus status;

	/**
	 * 예약 상태 변경
	 *
	 * @param newStatus 새로운 예약 상태
	 */
	public void updateStatus(ReservationStatus newStatus) {
		this.status = newStatus;
	}
}
