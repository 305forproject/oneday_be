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
import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;
import com.oneday.core.repository.ReservationRepository;
import com.oneday.core.repository.ReservationStatusRepository;
import com.oneday.core.repository.TimesRepository;
import com.oneday.core.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationService {
	private static final Integer CONFIRMED = 1; // "예약 확정"
	private static final Integer CANCELLED = 3; // "예약 취소"
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

		// 1. 사용자 조회 (없으면 404)
		User targetUser = userRepository.findById(studentId)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		// 2. 강의 시간 조회 (없으면 404)
		Times targetTime = timesRepository.findById(timeId)
				.orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

		// 3. 중복 예약 체크 (이미 결제 전 단계에서 했지만, 더블 체크)
		// -> 만약 그 사이에 중복이 발생했다면 409 Conflict 발생
		if (reservationRepository.existsByUser_IdAndTime_TimeIdAndStatus_StatusCode(
				studentId,
				timeId,
				CONFIRMED)) {
			throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
		}

		// 4. 정원 체크 (가장 중요)
		// -> 결제하고 들어왔는데 그 찰나에 자리가 다 찼으면 여기서 막아야 함
		long currentCount = reservationRepository.countByTime_TimeIdAndStatus_StatusCode(
				timeId,
				CONFIRMED
		);

		if (currentCount >= targetTime.getClasses().getMaxCapacity()) {
			throw new CustomException(ErrorCode.CLASS_CAPACITY_EXCEEDED);
		}

		// 5. 예약 상태 조회 (시스템 에러에 가까움)
		ReservationStatus confirmedStatus = reservationStatusRepository.findById(CONFIRMED)
				.orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
		// 혹은 ErrorCode.NOT_FOUND 같은 걸 써도 됩니다.

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
 	*
 	* @param studentId 조회할 학생의 ID
 	* @return 예정된 예약 목록과 지난 예약 목록을 담은 응답 DTO
 	*/
	@Transactional(readOnly = true)
	public StudentScheduleResponseDto getMyReservations(long studentId) {

		// 학생의 모든 예약 정보를 조회
		List<StudentReservationDto> allReservations =
				reservationRepository.findMyReservationsByStudentId(studentId);
		log.info("학생 예약 목록 조회 시작: studentId={}", studentId);

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

		// 분리된 리스트를 DTO에 담아 반환
		return new StudentScheduleResponseDto(
				partitionedSchedules.get(true),  // upcomingSchedules
				partitionedSchedules.get(false) // pastSchedules
		);
	}
}
