package com.oneday.core.service;

import org.springframework.stereotype.Service;

import com.oneday.core.repository.ClassRepository;
import com.oneday.core.repository.ReservationRepository;
import com.oneday.core.repository.ReservationStatusRepository;
import com.oneday.core.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 예약 서비스
 *
 * TODO: Entity 구조 변경으로 인한 리팩토링 필요
 * - Reservation.classes → Reservation.time 변경
 * - Times 엔티티 추가로 인한 로직 수정 필요
 *
 * @deprecated Entity 구조 변경으로 리팩토링 필요
 */
@Deprecated
@Service
@RequiredArgsConstructor
public class ReservationService {
	private static final Integer CONFIRMED = 1; // "예약 확정"
	private static final Integer CANCELLED = 2; // "예약 취소"
	private final ReservationRepository reservationRepository;
	private final ClassRepository classRepository;
	private final ReservationStatusRepository reservationStatusRepository;
	private final UserRepository userRepository;
	// 예약 확정 상태 번호
	// 추후 정해지면 변경 할 수도 안 할 수도

	// TODO: Entity 구조 변경으로 리팩토링 필요 - classes → time 변경
	/*
	public Reservation createReservation(int classId, long studentId) {

		User targetUser = userRepository.findById(studentId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

		Classes targetClass = classRepository.findById(classId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 강의입니다."));

		if ((reservationRepository.existsByUser_IdAndClasses_ClassIdAndStatus_StatusCode(
				studentId,
				classId,
				CONFIRMED))) {
			throw new RuntimeException("이미 예약한 강의입니다.");
		}

		long currentCount = reservationRepository.countByClasses_ClassIdAndStatus_StatusCode(
				classId,
				CONFIRMED
		);

		if (currentCount >= targetClass.getMaxCapacity()) {
			throw new RuntimeException("정원이 모두 마감되었습니다.");
		}

		ReservationStatus confirmedStatus = reservationStatusRepository.findById(CONFIRMED)
				.orElseThrow(() -> new RuntimeException("예약 상태 코드(ID: " + CONFIRMED + ")를 찾을 수 없습니다."));

		Reservation newReservation = Reservation.builder()
				.user(targetUser)
				.classes(targetClass)
				.status(confirmedStatus)
				.build();

		return reservationRepository.save(newReservation);
	}
	*/

	/**
	 * 예약을 취소
	 *
	 * @param reservationId 취소할 예약 ID
	 * @param studentId     취소를 요청한 사용자 ID
	 */
	// TODO: Entity 구조 변경으로 리팩토링 필요
	/*
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
	*/
}
