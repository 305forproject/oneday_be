package com.oneday.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oneday.core.entity.Categories;

/**
 * 카테고리 Repository
 * <p>
 * Categories 엔티티에 대한 데이터 접근 계층입니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Integer> {

	/**
	 * 카테고리명으로 카테고리 존재 여부 확인
	 *
	 * @param category 카테고리명
	 * @return 존재 여부
	 */
	boolean existsByCategory(String category);

	/**
	 * 카테고리명으로 카테고리 조회
	 *
	 * @param category 카테고리명 (한글)
	 * @return 카테고리 엔티티
	 */
	Optional<Categories> findByCategory(String category);
}

