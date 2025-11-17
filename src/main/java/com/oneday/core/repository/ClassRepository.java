package com.oneday.core.repository;

import java.util.List;

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
	 * 검색어가 클래스 이름이나 강사 이름에 포함된 목록 조회
	 */
	@Query("SELECT c FROM Classes c " +
			"JOIN FETCH c.teacher " +
			"JOIN FETCH c.category " +
			"WHERE c.className LIKE CONCAT('%', :keyword, '%') OR c.teacher.name LIKE CONCAT('%', :keyword, '%')")
	List<Classes> searchByKeyword(@Param("keyword") String keyword);
}
