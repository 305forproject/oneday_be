package com.oneday.core.service;

import org.springframework.stereotype.Service;

import com.oneday.core.dto.ReservationRequestDto;
import com.oneday.core.entity.Classes;
import com.oneday.core.entity.Reservation;
import com.oneday.core.entity.ReservationStatus;
import com.oneday.core.repository.ClassRepository;
import com.oneday.core.repository.ReservationRepository;
import com.oneday.core.repository.ReservationStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {
	private final ReservationRepository reservationRepository;
	private final ClassRepository classRepository;
	private final ReservationStatusRepository reservationStatusRepository;

	private static final int CONFIRMED = 1;
	// 예약 확정 상태 번호
	// 추후 정해지면 변경 할 수도 안 할 수도

	public Reservation createReservation(int classId, int studentId) {
		if (dto == null) {
			throw new RuntimeException("예약 정보가 필요합니다.");
		}

		User targetUser = userRepository.findById(studentId)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

		Classes targetClass = classRepository.findById(dto.getClassId())
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
			.status(pendingStatus)
			.build();

		return reservationRepository.save(newReservation);
	}

}
