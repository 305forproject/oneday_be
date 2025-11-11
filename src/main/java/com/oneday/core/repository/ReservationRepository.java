package com.oneday.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneday.core.entity.Reservation;

import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

	boolean existsByUser_IdAndClasses_ClassIdAndStatus_StatusCode(
		long studentId,
		Integer classId,
		Integer statusCode
	);

	long countByClasses_ClassIdAndStatus_StatusCode(
        Integer classId,
        Integer statusCode
    );
}
