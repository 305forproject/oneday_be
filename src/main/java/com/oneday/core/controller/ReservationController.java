package com.oneday.core.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.dto.reservation.ReservationRequestDto;
import com.oneday.core.dto.student.StudentScheduleResponseDto;
import com.oneday.core.entity.Reservation;
import com.oneday.core.exception.ErrorCode;
import com.oneday.core.service.ReservationService;

@Slf4j
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
	private final ReservationService reservationService;

	@PostMapping
	public ResponseEntity<?> createReservation(@RequestBody ReservationRequestDto reservationDto, HttpSession session) {
		if (session == null || session.getAttribute("userId") == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다."));
		}
		long studentId = (Long)session.getAttribute("userId");

		try {
			Reservation createdReservation = reservationService.createReservation(
					reservationDto.getTimeId(),
					studentId
			);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
		} catch (RuntimeException e) {
			log.info("예약 생성 실패: {}", e.getMessage());
			return ResponseEntity.badRequest()
					.body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * 예약 취소
	 * @param reservationId 취소할 예약의 ID
	 */
	@PatchMapping("/{reservationId}/cancel")
	public ResponseEntity<?> cancelReservation(
			@PathVariable int reservationId,
			HttpSession session) {

		if (session == null || session.getAttribute("userId") == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다."));
		}

		long studentId = (Long)session.getAttribute("userId");

		try {
			Reservation cancelledReservation = reservationService.cancelReservation(reservationId, studentId);
			return ResponseEntity.ok(cancelledReservation);

		} catch (RuntimeException e) {
			log.info("예약 취소 실패: {}", e.getMessage());
			return ResponseEntity.badRequest()
					.body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * 학생 본인의 예약 목록 조회
	 */
	@GetMapping("/my")
	public ResponseEntity<ApiResponse<?>> getMyReservations(HttpSession session) {

		// if (session == null || session.getAttribute("userId") == null) {
		// 	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
		// 			.body(Map.of("message", "로그인이 필요합니다."));
		// }

		// 추후 토큰에서 가져오는 걸로 수정
		long studentId = (Long) session.getAttribute("userId");

		try {
			StudentScheduleResponseDto response = reservationService.getMyReservations(studentId);
			return ResponseEntity.ok(ApiResponse.success(response));

		} catch (Exception e) {
			log.error("내 예약 조회 중 오류 발생 (ID: {})", studentId, e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));

		}
	}
}
