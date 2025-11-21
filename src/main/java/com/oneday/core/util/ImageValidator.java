package com.oneday.core.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.oneday.core.constants.ImageConstants;
import com.oneday.core.exception.classes.InvalidImageException;

import lombok.extern.slf4j.Slf4j;

/**
 * 이미지 검증 유틸리티
 *
 * @author zionge2k
 * @since 2025-01-27
 */
@Slf4j
@Component
public class ImageValidator {

	/**
	 * 이미지 개수 검증
	 *
	 * @param files 업로드할 파일 배열
	 * @throws InvalidImageException 이미지 개수가 유효하지 않은 경우
	 */
	public void validateImageCount(MultipartFile[] files) {
		if (files == null || files.length < ImageConstants.MIN_IMAGE_COUNT || files.length > ImageConstants.MAX_IMAGE_COUNT) {
			throw new InvalidImageException("이미지는 " + ImageConstants.MIN_IMAGE_COUNT + "개 이상 " + ImageConstants.MAX_IMAGE_COUNT + "개 이하로 등록해야 합니다");
		}
	}

	/**
	 * 이미지 파일 검증
	 *
	 * @param file 검증할 파일
	 * @throws InvalidImageException 파일이 유효하지 않은 경우
	 */
	public void validateImageFile(MultipartFile file) {
		if (file.isEmpty()) {
			throw new InvalidImageException("빈 파일은 업로드할 수 없습니다");
		}

		if (file.getSize() > ImageConstants.MAX_FILE_SIZE) {
			throw new InvalidImageException("파일 크기는 5MB를 초과할 수 없습니다: " + file.getOriginalFilename());
		}

		String extension = getFileExtension(file.getOriginalFilename());
		if (!ImageConstants.ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new InvalidImageException("지원하지 않는 파일 형식입니다: " + extension);
		}
	}

	/**
	 * 파일 확장자 추출
	 *
	 * @param filename 파일명
	 * @return 파일 확장자 (소문자)
	 * @throws InvalidImageException 파일명이 유효하지 않은 경우
	 */
	public String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			throw new InvalidImageException("유효하지 않은 파일명입니다");
		}
		return filename.substring(filename.lastIndexOf('.') + 1);
	}
}

