package com.oneday.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneday.core.dto.EnrolledStudentDto;
import com.oneday.core.entity.Reservation;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

	boolean existsByUser_IdAndTime_TimeIdAndStatus_StatusCode(
			long studentId,
			Integer timeId,
			Integer statusCode
	);

	long countByTime_TimeIdAndStatus_StatusCode(
			Integer timeId,
			Integer statusCode
	);

	// 특정 수업 시간(timeId)에 '예약 확정'된 수강생 목록을 DTO로 조회합니다.
	@Query("SELECT new com.oneday.core.dto.EnrolledStudentDto(" +
			"s.id, s.name, s.email) " +
			"FROM Reservation r " +
			"JOIN r.user s " +
			"JOIN r.time t " +
			"WHERE t.timeId = :timeId " +
			"AND r.status.statusCode = :confirmedStatusId " +
			"AND t.classes.teacher.id = :teacherId")
	List<EnrolledStudentDto> findEnrolledStudentsByTime(
			@Param("teacherId") long teacherId,
			@Param("timeId") int timeId,
			@Param("confirmedStatusId") int confirmedStatusId
	);
}
