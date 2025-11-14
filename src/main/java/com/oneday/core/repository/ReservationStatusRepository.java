package com.oneday.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneday.core.entity.ReservationStatus;

public interface ReservationStatusRepository extends JpaRepository<ReservationStatus, Integer> {
}