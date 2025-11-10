package com.oneday.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oneday.core.model.Classes;

@Repository
public interface ClassRepository extends JpaRepository<Classes, Integer> {
}
