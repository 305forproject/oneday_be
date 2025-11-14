package com.oneday.core.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.oneday.core.dto.EnrolledStudentDto;
import com.oneday.core.dto.TeacherScheduleDto;
import com.oneday.core.entity.Times;

@Repository
public interface TimesRepository extends JpaRepository<Times, Integer> {

	// 수업 및 강사 정보 조회 쿼리
	@Query("SELECT new com.oneday.core.dto.TeacherScheduleDto(" +
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
			"AND t.classes.teacher.id = :teacherId " +
			"GROUP BY t.timeId")
	List<Object[]> findAllConfirmedReservationCounts(
			@Param("teacherId") long teacherId,
			@Param("confirmedStatusId") int confirmedStatusId
	);

}
