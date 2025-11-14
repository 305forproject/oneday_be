package com.oneday.core.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneday.core.dto.student.StudentReservationDto;
import com.oneday.core.dto.student.StudentScheduleResponseDto;
import com.oneday.core.entity.Reservation;
import com.oneday.core.entity.ReservationStatus;
import com.oneday.core.entity.Times;
import com.oneday.core.entity.User;
import com.oneday.core.repository.ReservationRepository;
import com.oneday.core.repository.ReservationStatusRepository;
import com.oneday.core.repository.TimesRepository;
import com.oneday.core.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {
	private static final Integer CONFIRMED = 1; // "예약 확정"
	private static final Integer CANCELLED = 2; // "예약 취소"
	private final ReservationRepository reservationRepository;
	private final TimesRepository timesRepository;
	private final ReservationStatusRepository reservationStatusRepository;
	private final UserRepository userRepository;
	// 예약 확정 상태 번호
	// 추후 정해지면 변경 할 수도 안 할 수도

	/**
	 * 예약 생성
	 *
	 * @param timeId    예약할 강의 시간 ID
	 * @param studentId 예약을 생성할 학생 ID
	 * @return 생성된 예약 정보
	 * @throws RuntimeException 사용자/시간/상태를 찾을 수 없거나, 중복 예약, 정원 초과 시 발생
	 */
	public Reservation createReservation(int timeId, long studentId) {

		User targetUser = userRepository.findById(studentId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

		Times targetTime = timesRepository.findById(timeId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 강의 시간입니다."));

		if ((reservationRepository.existsByUser_IdAndTime_TimeIdAndStatus_StatusCode(
				studentId,
				timeId,
				CONFIRMED))) {
			throw new RuntimeException("이미 예약한 강의입니다.");
		}

		long currentCount = reservationRepository.countByTime_TimeIdAndStatus_StatusCode(
				timeId,
				CONFIRMED
		);

		if (currentCount >= targetTime.getClasses().getMaxCapacity()) {
			throw new RuntimeException("정원이 모두 마감되었습니다.");
		}

		ReservationStatus confirmedStatus = reservationStatusRepository.findById(CONFIRMED)
				.orElseThrow(() -> new RuntimeException("예약 상태 코드(ID: " + CONFIRMED + ")를 찾을 수 없습니다."));

		Reservation newReservation = Reservation.builder()
				.user(targetUser)
				.time(targetTime)
				.status(confirmedStatus)
				.build();

		return reservationRepository.save(newReservation);
	}

	/**
	 * 예약을 취소
	 *
	 * @param reservationId 취소할 예약 ID
	 * @param studentId     취소를 요청한 사용자 ID
	 */
	@Transactional
	public Reservation cancelReservation(int reservationId, long studentId) {
		ReservationStatus cancelledStatus = reservationStatusRepository.findById(CANCELLED)
				.orElseThrow(() -> new RuntimeException("취소 상태 코드(ID: " + CANCELLED + ")를 찾을 수 없습니다."));

		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 예약입니다."));

		// 1. 본인 확인: 예약에 저장된 user ID와 세션의 studentId가 일치하는지 확인
		if (reservation.getUser().getId() != studentId) {
			throw new RuntimeException("본인의 예약만 취소할 수 있습니다.");
		}

		// 2. 이미 취소된 예약인지 확인
		if (reservation.getStatus().getStatusCode().equals(CANCELLED)) {
			throw new RuntimeException("이미 취소된 예약입니다.");
		}

		// 3. 확정 상태가 맞는지 확인
		if (!reservation.getStatus().getStatusCode().equals(CONFIRMED)) {
			throw new RuntimeException("예약 확정 상태가 아니므로 취소할 수 없습니다.");
		}

		// 4. 상태 변경: 예약 객체의 상태를 "취소"(2)로 업데이트
		reservation.updateStatus(cancelledStatus);

		// @Transactional -> 메소드가 끝나면
		// reservation 객체 변경 감지(Dirty Checking)
		// 자동으로 UPDATE 쿼리 실행 (save 호출 불필요)

		return reservation;
	}

	/**
	 * 학생의 '내 예약 목록'을 '예정된'/'지난'으로 분리하여 조회
	 */
	@Transactional(readOnly = true)
	public StudentScheduleResponseDto getMyReservations(long studentId) {

		// 학생의 모든 예약 정보를 조회
		List<StudentReservationDto> allReservations =
				reservationRepository.findMyReservationsByStudentId(studentId);

		if (allReservations.isEmpty()) {
			return new StudentScheduleResponseDto(List.of(), List.of());
		}

		// 현재 시간 기준으로 '예정'/'지난' 수업 분리
		LocalDateTime now = LocalDateTime.now();

		Map<Boolean, List<StudentReservationDto>> partitionedSchedules =
				allReservations.stream()
						.collect(Collectors.partitioningBy(
								schedule -> schedule.getStartAt().isAfter(now)
						));

		// 3. 분리된 리스트를 DTO에 담아 반환
		return new StudentScheduleResponseDto(
				partitionedSchedules.get(true),  // upcomingSchedules
				partitionedSchedules.get(false) // pastSchedules
		);
	}
}
