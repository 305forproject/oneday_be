package com.oneday.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation_status")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "status_code")
	private Integer statusCode;

	@Column(name = "status_name", nullable = false)
	private String statusName;
}