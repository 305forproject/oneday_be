package com.oneday.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.oneday.core.entity.Classes;

@Repository
public interface ClassRepository extends JpaRepository<Classes, Integer> {

	/**
	 * N+1 방지를 위해 teacher, category를 Fetch Join
	 */
	@Query("SELECT c FROM Classes c JOIN FETCH c.teacher JOIN FETCH c.category")
	List<Classes> findAllWithTeacherAndCategory();

	/**
	 * 카테고리, 정렬, 검색어
	 * @param categoryId 카테고리 ID
	 * @param keyword 검색어
	 * @param sort 정렬 기준
	 * @return 검색 결과
	 */
	@Query("SELECT c FROM Classes c " +
			"JOIN FETCH c.teacher " +
			"JOIN FETCH c.category " +
			"WHERE (:categoryId IS NULL OR c.category.categoryId = :categoryId) " +
			"AND (:keyword IS NULL OR c.className LIKE CONCAT('%', :keyword, '%') OR c.teacher.name LIKE CONCAT('%', :keyword, '%'))")
	List<Classes> findAllByConditions(
			@Param("categoryId") Integer categoryId,
			@Param("keyword") String keyword,
			Sort sort
	);

	/**
	 * 클래스 + 강사 + 카테고리 한 번에 조회
	 */
	@Query("SELECT c FROM Classes c " +
			"JOIN FETCH c.teacher " +
			"JOIN FETCH c.category " +
			"WHERE c.classId = :classId")
	Optional<Classes> findByIdWithDetails(@Param("classId") Integer classId);
}
