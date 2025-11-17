package com.oneday.core.config;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.oneday.core.entity.Categories;
import com.oneday.core.entity.CategoryType;
import com.oneday.core.repository.CategoriesRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션 시작 시 Categories 테이블 초기화
 * <p>
 * Categories 테이블이 비어있으면 CategoryType Enum의 모든 카테고리를 자동으로 등록합니다.
 * 중복 초기화를 방지하기 위해 데이터 존재 여부를 확인합니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryInitializer {

	private final CategoriesRepository categoriesRepository;

	/**
	 * Categories 테이블이 비어있으면 초기 데이터 삽입
	 * <p>
	 * 애플리케이션 시작 시 자동으로 실행됩니다.
	 * </p>
	 */
	@PostConstruct
	@Transactional
	public void initCategories() {
		long count = categoriesRepository.count();

		if (count == 0) {
			log.info("Categories 테이블 초기화 시작");

			// 모든 카테고리 엔티티를 리스트로 생성 후 배치 저장
			var categories = Arrays.stream(CategoryType.values())
					.map(type -> Categories.builder()
							.category(type.getKoreanName())
							.build())
					.toList();
			categoriesRepository.saveAll(categories);

			log.info("Categories 테이블 초기화 완료: {} 개", categories.size());
		} else {
			log.info("Categories 테이블 이미 초기화됨: {} 개", count);
		}
	}
}

