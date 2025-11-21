package com.oneday.core.constants;

import java.util.Arrays;
import java.util.List;

/**
 * 이미지 관련 상수
 *
 * @author zionge2k
 * @since 2025-01-27
 */
public final class ImageConstants {

	/**
	 * 허용된 이미지 확장자 목록
	 */
	public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

	/**
	 * 최대 파일 크기 (5MB)
	 */
	public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

	/**
	 * 최소 이미지 개수
	 */
	public static final int MIN_IMAGE_COUNT = 1;

	/**
	 * 최대 이미지 개수
	 */
	public static final int MAX_IMAGE_COUNT = 8;

	/**
	 * 인스턴스 생성 방지
	 */
	private ImageConstants() {
		throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다");
	}
}

