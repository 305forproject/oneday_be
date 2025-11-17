package com.oneday.core.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;
import com.oneday.core.service.ReservationService;

@Slf4j
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
	private final ReservationService reservationService;

	/**
	 * 예약 생성
	 * @param reservationDto 예약 정보
	 * @param authenticationprincipal 인증된 사용자 정보
	 * @return 생성된 예약
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<Reservation>> createReservation(
			@RequestBody ReservationRequestDto reservationDto,
			@AuthenticationPrincipal Long studentId) {

		if (studentId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
		}

		log.info("인증된 사용자 ID: {}", studentId);

		try {
			Reservation createdReservation = reservationService.createReservation(
					reservationDto.getTimeId(),
					studentId
			);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(createdReservation));

		} catch (CustomException e) {
			log.warn("예약 생성 실패: {}", e.getMessage());
			return ResponseEntity.status(e.getErrorCode().getStatus())
					.body(ApiResponse.error(e.getErrorCode()));
		} catch (Exception e) {
			log.error("예약 생성 중 예상치 못한 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}

	/**
	 * 예약 취소
	 *
	 * @param reservationId 취소할 예약의 ID
	 * @param authenticationprincipal 인증된 사용자 정보
	 */
	@PatchMapping("/{reservationId}/cancel")
	public ResponseEntity<ApiResponse<Reservation>> cancelReservation(
			@PathVariable int reservationId,
			@AuthenticationPrincipal Long studentId) {

		if (studentId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
		}

		log.info("인증된 사용자 ID: {}", studentId);

		try {
			Reservation cancelledReservation = reservationService.cancelReservation(reservationId, studentId);

			return ResponseEntity.ok(ApiResponse.success(cancelledReservation));

		} catch (CustomException e) {
			log.warn("예약 취소 실패: {}", e.getMessage());
			return ResponseEntity.status(e.getErrorCode().getStatus())
					.body(ApiResponse.error(e.getErrorCode()));
		} catch (Exception e) {
			log.error("예약 취소 중 예상치 못한 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}

	/**
 	* 학생 본인의 예약 목록 조회
 	*
	* @param authenticationprincipal 인증된 사용자 정보
 	* @return 예정된 예약과 지난 예약이 포함된 응답
 	*/
	@GetMapping("/my")
	public ResponseEntity<ApiResponse<StudentScheduleResponseDto>> getMyReservations(@AuthenticationPrincipal Long studentId) {

		if (studentId == null) {
			ErrorCode unauthorizedError = ErrorCode.UNAUTHORIZED;

			return ResponseEntity.status(unauthorizedError.getStatus())
					.body(ApiResponse.error(unauthorizedError));
		}

		log.info("인증된 사용자 ID: {}", studentId);

		try {
			StudentScheduleResponseDto response = reservationService.getMyReservations(studentId);
			return ResponseEntity.ok(ApiResponse.success(response));

		} catch (com.oneday.core.exception.CustomException e) {
			log.warn("내 예약 조회 실패 (ID: {}): {}", studentId, e.getMessage());
			return ResponseEntity.status(e.getErrorCode().getStatus())
					.body(ApiResponse.error(e.getErrorCode()));
		} catch (Exception e) {
			log.error("내 예약 조회 중 예상치 못한 오류 발생 (ID: {})", studentId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}
}
