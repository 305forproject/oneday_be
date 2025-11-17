package com.oneday.core.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.oneday.core.dto.teacher.TeacherScheduleDto;
import com.oneday.core.entity.Times;

@Repository
public interface TimesRepository extends JpaRepository<Times, Integer> {

	// 수업 및 강사 정보 조회 쿼리
	@Query("SELECT new com.oneday.core.dto.teacher.TeacherScheduleDto(" +
			"c.classId, c.className, c.location, c.longitude, c.latitude, c.maxCapacity, " +
			"t.timeId, t.startAt, t.endAt, " +
			"0L) " + // 예약자 수는 0으로 초기화
			"FROM Times t " +
			"JOIN t.classes c " +
			"JOIN c.teacher teacher " +
			"WHERE teacher.id = :teacherId  " +
			"ORDER BY t.startAt ASC")
	List<TeacherScheduleDto> findAllTeacherSchedulesWithoutReservationCount(
			@Param("teacherId") long teacherId
	);

	// 예약 확정 수 집계 쿼리
	@Query("SELECT t.timeId, COUNT(r.reservationId) " +
			"FROM Reservation r " +
			"JOIN r.time t " +
			"WHERE r.status.statusCode = :confirmedStatusId " +
			"AND t.startAt > :currentTime " +
			"AND t.classes.teacher.id = :teacherId " +
			"GROUP BY t.timeId")
	List<Object[]> findAllConfirmedReservationCounts(
			@Param("teacherId") long teacherId,
			@Param("confirmedStatusId") int confirmedStatusId
	);

	/**
	 * 특정 클래스의 시간대 중복 체크 (Phase 3 - 클래스 등록)
	 * <p>
	 * 새로운 시간대가 기존 시간대와 겹치는지 확인합니다.
	 * 겹침 조건: (새 시작 < 기존 종료) AND (새 종료 > 기존 시작)
	 * </p>
	 *
	 * @param classId 클래스 ID
	 * @param startAt 시작 시간
	 * @param endAt 종료 시간
	 * @return 겹치는 시간대가 존재하면 true
	 */
	@Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
			"FROM Times t " +
			"WHERE t.classes.classId = :classId " +
			"AND t.startAt < :endAt AND t.endAt > :startAt")
	boolean existsOverlappingTimes(
			@Param("classId") Integer classId,
			@Param("startAt") LocalDateTime startAt,
			@Param("endAt") LocalDateTime endAt
	);

	/**
	 * 특정 강사의 시간대 중복 체크 (Phase 4 - 클래스 등록)
	 * <p>
	 * 강사의 모든 클래스를 대상으로 시간 중복을 검사합니다.
	 * 겹침 조건: (새 시작 < 기존 종료) AND (새 종료 > 기존 시작)
	 * </p>
	 *
	 * @param teacherId 강사 ID
	 * @param startAt   시작 시간
	 * @param endAt     종료 시간
	 * @return 겹치는 시간대가 존재하면 true
	 */
	@Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
			"FROM Times t " +
			"WHERE t.classes.teacher.id = :teacherId " +
			"AND t.startAt < :endAt AND t.endAt > :startAt")
	boolean existsOverlappingTimesByTeacher(
			@Param("teacherId") Long teacherId,
			@Param("startAt") LocalDateTime startAt,
			@Param("endAt") LocalDateTime endAt
	);

}
