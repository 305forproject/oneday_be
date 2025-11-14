package com.oneday.core.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneday.core.dto.EnrolledStudentDto;
import com.oneday.core.dto.TeacherScheduleDto;
import com.oneday.core.dto.TeacherScheduleResponseDto;
import com.oneday.core.repository.ReservationRepository;
import com.oneday.core.repository.TimesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeacherService {

	// 예약 확정 상태 코드 (status_code = 1)
	private static final int CONFIRMED_STATUS_ID = 1;
	private final TimesRepository timesRepository;
	private final ReservationRepository reservationRepository;

	@Transactional(readOnly = true)
	public TeacherScheduleResponseDto getTeacherSchedule(long teacherId) {

		// 강사의 모든 스케줄 정보 조회
		List<TeacherScheduleDto> allSchedules = timesRepository.findAllTeacherSchedulesWithoutReservationCount(
				teacherId);

		if (allSchedules.isEmpty()) {
			// 빈 리스트라도 DTO 구조에 맞춰 반환
			return new TeacherScheduleResponseDto(List.of(), List.of());
		}

		// 예약 확정 수 집계
		List<Object[]> counts = timesRepository.findAllConfirmedReservationCounts(
				teacherId,
				CONFIRMED_STATUS_ID
		);
		Map<Integer, Long> countMap = counts.stream()
				.collect(Collectors.toMap(
						row -> (Integer)row[0],
						row -> (Long)row[1]
				));

		// 예약자 수를 DTO에 채워넣기
		allSchedules.forEach(dto -> {
			long count = countMap.getOrDefault(dto.getTimeId(), 0L);
			dto.setConfirmedStudentCount(count);
		});

		// '예정' / '지난' 수업 분리

		LocalDateTime now = LocalDateTime.now();

		// Java Stream의 partitioningBy를 사용해 두 리스트로 자동 분리
		Map<Boolean, List<TeacherScheduleDto>> partitionedSchedules = allSchedules.stream()
				.collect(Collectors.partitioningBy(
						schedule -> schedule.getStartAt().isAfter(now)
				));

		// 분리된 리스트를 새 DTO에 담아 반환
		return new TeacherScheduleResponseDto(
				partitionedSchedules.get(true),  // 예정된 클래스 리스트
				partitionedSchedules.get(false) // 지난 클래스 리스트
		);
	}

	/**
	 * 특정 수업의 수강생 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<EnrolledStudentDto> getEnrolledStudents(long teacherId, int timeId) {
		return reservationRepository.findEnrolledStudentsByTime(
				teacherId,
				timeId,
				CONFIRMED_STATUS_ID
		);
	}
}
