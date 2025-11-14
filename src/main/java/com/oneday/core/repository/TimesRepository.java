package com.oneday.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oneday.core.entity.Times;

@Repository
public interface TimesRepository extends JpaRepository<Times, Integer> {

}
