package com.oneday.core.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 클래스 카테고리 타입
 * <p>
 * 8가지 카테고리를 정의합니다.
 * 한글명과 영문명을 모두 제공하여 향후 다국어 대응을 고려합니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Getter
@RequiredArgsConstructor
public enum CategoryType {

	/**
	 * 건강/뷰티 카테고리
	 */
	HEALTH_BEAUTY("건강/뷰티", "Health & Beauty"),

	/**
	 * 공예/예술 카테고리
	 */
	CRAFT_ART("공예/예술", "Craft & Art"),

	/**
	 * 스포츠/레저 카테고리
	 */
	SPORTS_LEISURE("스포츠/레저", "Sports & Leisure"),

	/**
	 * 요리/베이킹 카테고리
	 */
	COOKING_BAKING("요리/베이킹", "Cooking & Baking"),

	/**
	 * 음악/댄스 카테고리
	 */
	MUSIC_DANCE("음악/댄스", "Music & Dance"),

	/**
	 * 언어/교육 카테고리
	 */
	LANGUAGE_EDUCATION("언어/교육", "Language & Education"),

	/**
	 * IT/기술 카테고리
	 */
	IT_TECHNOLOGY("IT/기술", "IT & Technology"),

	/**
	 * 라이프스타일 카테고리
	 */
	LIFESTYLE("라이프스타일", "Lifestyle");

	/**
	 * 한글 카테고리명
	 */
	private final String koreanName;

	/**
	 * 영문 카테고리명
	 */
	private final String englishName;
}

