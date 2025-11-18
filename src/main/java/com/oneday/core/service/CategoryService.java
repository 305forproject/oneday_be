package com.oneday.core.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneday.core.dto.category.CategoryResponseDto;
import com.oneday.core.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

	private final CategoryRepository categoryRepository;

	/**
	 * 모든 카테고리 조회
	 *
	 * @return dto 반환
	 */
	public List<CategoryResponseDto> getAllCategories() {
		return categoryRepository.findAll().stream()
				.map(CategoryResponseDto::from)
				.collect(Collectors.toList());
	}
}
