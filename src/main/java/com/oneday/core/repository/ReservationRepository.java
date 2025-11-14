package com.oneday.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneday.core.entity.Reservation;

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
}
