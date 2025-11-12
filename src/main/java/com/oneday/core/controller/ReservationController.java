package com.oneday.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneday.core.service.ReservationService;

import lombok.RequiredArgsConstructor;

/**
 * 예약 컨트롤러
 *
 * TODO: Entity 구조 변경으로 인한 리팩토링 필요
 * - Reservation.classes → Reservation.time 변경
 * - Times 엔티티 추가로 인한 로직 수정 필요
 *
 * @deprecated Entity 구조 변경으로 리팩토링 필요
 */
@Deprecated
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
	private static final Logger log = LoggerFactory.getLogger(ReservationController.class);
	private final ReservationService reservationService;

	// TODO: Entity 구조 변경으로 리팩토링 필요
	// 모든 메서드를 주석 처리합니다.
}
