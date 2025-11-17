package com.oneday.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oneday.core.entity.Images;

public interface ImageRepository extends JpaRepository<Images, Integer> {

	/**
	 * 모든 대표 이미지 조회
	 */
	@Query("SELECT i FROM Images i JOIN FETCH i.classes WHERE i.isRepresentative = true")
	List<Images> findAllRepresentativeImages();

	/**
	 * 특정 클래스 ID 목록에 해당하는 대표 이미지 조회
	 */
	@Query("SELECT i FROM Images i WHERE i.classes.classId IN :classIds AND i.isRepresentative = true")
	List<Images> findRepresentativeImagesByClassIds(@Param("classIds") List<Integer> classIds);
}
