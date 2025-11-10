package com.oneday.core.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneday.core.dto.ReservationRequestDto;
import com.oneday.core.entity.Reservation;
import com.oneday.core.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
	private final ReservationService reservationService;
	private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

	@PostMapping
	public ResponseEntity<?> createReservation(@RequestBody ReservationRequestDto reservationDto, HttpSession session) {
		if (session == null || session.getAttribute("userId") == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("message", "로그인이 필요합니다."));
		}
		int studentId = (Integer) session.getAttribute("userId");

		try {
			Reservation createdReservation = reservationService.createReservation(
            	reservationDto.getClassId(), 
            	studentId
        	);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
		} catch (RuntimeException e) {
			log.info("예약 생성 실패: {}", e.getMessage());
			return ResponseEntity.badRequest()
				.body(Map.of("message", e.getMessage()));
		}
	}
}
