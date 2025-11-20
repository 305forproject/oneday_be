package com.oneday.core.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import com.oneday.core.entity.ReservationStatus;
import com.oneday.core.repository.ReservationRepository;
import com.oneday.core.repository.ReservationStatusRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationScheduler {

	private final ReservationRepository reservationRepository;
	private final ReservationStatusRepository reservationStatusRepository;

	public static final Integer CONFIRMED = 1;       // 예약 완료
	public static final Integer COMPLETED = 4; // 수강 완료

	@Scheduled(cron = "0 0/30 * * * *")
	@Transactional
	public void completeFinishedClasses() {
		log.info("수강 완료 처리 스케줄러 시작 - {}", LocalDateTime.now());

		// [변경 포인트 1] 상수 사용 & DB 조회 없이 프록시 객체만 획득
		// getReferenceById는 실제 DB를 조회하지 않고, ID값만 가진 가짜 객체(Proxy)를 즉시 반환합니다.
		ReservationStatus reservedStatus = reservationStatusRepository.getReferenceById(CONFIRMED);
		ReservationStatus completedStatus = reservationStatusRepository.getReferenceById(COMPLETED);

		LocalDateTime now = LocalDateTime.now();

		int updatedCount = reservationRepository.updateStatusForCompletedClasses(
				reservedStatus,
				completedStatus,
				now
		);

		log.info("수강 완료 처리 완료. 총 {}건 업데이트 됨.", updatedCount);
	}
}
