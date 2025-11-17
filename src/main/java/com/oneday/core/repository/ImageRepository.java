package com.oneday.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.oneday.core.entity.Images;

public interface ImageRepository extends JpaRepository<Images, Integer> {

	/**
	 * 모든 대표 이미지 조회
	 */
	@Query("SELECT i FROM Images i WHERE i.isRepresentative = true")
	List<Images> findAllRepresentativeImages();
}
