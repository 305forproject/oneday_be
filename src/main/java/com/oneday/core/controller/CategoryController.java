package com.oneday.core.controller;

import com.oneday.core.dto.category.CategoryResponseDto;
import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.service.CategoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	/**
	 * 모든 카테고리 조회
	 *
	 * @return dto 반환
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories() {
		List<CategoryResponseDto> categories = categoryService.getAllCategories();
		return ResponseEntity.ok(ApiResponse.success(categories));
	}
}
