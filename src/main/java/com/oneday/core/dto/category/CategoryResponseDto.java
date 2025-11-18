package com.oneday.core.dto.category;

import com.oneday.core.entity.Categories;

public record CategoryResponseDto(
		Integer categoryId,
		String categoryName
) {
	public static CategoryResponseDto from(Categories entity) {
		return new CategoryResponseDto(
				entity.getCategoryId(),
				entity.getCategory()
		);
	}
}
