package com.oneday.core.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneday.core.dto.student.StudentReservationDto;
import com.oneday.core.dto.teacher.EnrolledStudentDto;
import com.oneday.core.entity.Reservation;
import com.oneday.core.entity.ReservationStatus;

import org.springframework.data.jpa.repository.Modifying;
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
	@Query("SELECT new com.oneday.core.dto.teacher.EnrolledStudentDto(" +
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

	/**
	 * 학생 ID를 기준으로 예약 목록 전체를 DTO 조회
	 *
	 * @param studentId 조회할 학생의 ID
	 * @return 학생의 전체 예약 목록 (최신순 정렬)
	 */
	@Query("SELECT new com.oneday.core.dto.student.StudentReservationDto(" +
			"c.classId, c.className, c.location, c.longitude, c.latitude, c.price, " +
			"t.timeId, t.startAt, t.endAt, " +
			"teacher.name, teacher.email, " +
			"r.reservationId , s.statusCode, s.statusName) " +
			"FROM Reservation r " +
			"JOIN r.user student " +
			"JOIN r.status s " +
			"JOIN r.time t " +
			"JOIN t.classes c " +
			"JOIN c.teacher teacher " +
			"WHERE student.id = :studentId " +
			"ORDER BY t.startAt DESC")
	List<StudentReservationDto> findMyReservationsByStudentId(
			@Param("studentId") long studentId
	);

	/**
	 * Bulk Update:
	 * 조건: 현재 상태가 oldStatus(1) 이고, 수업 종료 시간(endAt)이 현재 시간(now)보다 과거인 경우
	 * 동작: 상태를 newStatus(4)로 변경
	 */
	@Modifying(clearAutomatically = true) // 쿼리 실행 후 영속성 컨텍스트 초기화 (데이터 불일치 방지)
	@Query("UPDATE Reservation r " +
			"SET r.status = :newStatus " +
			"WHERE r.status = :oldStatus " +
			"AND r.time.endAt < :now")
	int updateStatusForCompletedClasses(@Param("oldStatus") ReservationStatus oldStatus,
			@Param("newStatus") ReservationStatus newStatus,
			@Param("now") LocalDateTime now);
}
