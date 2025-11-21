package com.oneday.core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneday.core.dto.category.CategoryResponseDto;
import com.oneday.core.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

	private final CategoryRepository categoryRepository;

	/**
	 * 모든 카테고리 조회
	 *
	 * @return 카테고리 목록 DTO
	 */
	public List<CategoryResponseDto> getAllCategories() {
		List<CategoryResponseDto> categories = categoryRepository.findAll().stream()
			.map(CategoryResponseDto::from)
			.toList();

		log.info("카테고리 조회 완료: {}개", categories.size());
		return categories;
	}
}
