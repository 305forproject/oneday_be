package com.oneday.core.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oneday.core.exception.classes.InvalidImageException;

import lombok.extern.slf4j.Slf4j;

/**
 * 로컬 파일 시스템 이미지 업로드 서비스
 *
 * @author zionge2k
 * @since 2025-01-27
 */
@Slf4j
@Service
public class LocalImageService {

	@Value("${file.upload.base-dir}")
	private String baseDir;

	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

	/**
	 * 이미지 파일 로컬 저장
	 *
	 * @param files 업로드할 파일 배열
	 * @param classId 클래스 ID
	 * @param primaryIndex 대표 이미지 인덱스
	 * @return 저장된 이미지 URL 목록
	 */
	public List<String> uploadImages(MultipartFile[] files, Integer classId, int primaryIndex) {
		validateImageCount(files);

		Path uploadPath = createUploadDirectory(classId);
		List<String> imageUrls = new ArrayList<>();

		for (int i = 0; i < files.length; i++) {
			MultipartFile file = files[i];
			validateImageFile(file);

			String fileName = generateFileName(file.getOriginalFilename());
			Path filePath = uploadPath.resolve(fileName);

			try {
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				String imageUrl = buildImageUrl(classId, fileName);
				imageUrls.add(imageUrl);
				log.info("로컬 이미지 저장 완료: {}, isPrimary={}", imageUrl, i == primaryIndex);
			} catch (IOException e) {
				deleteUploadedFiles(imageUrls);
				throw new InvalidImageException("이미지 저장 중 오류가 발생했습니다: " + file.getOriginalFilename());
			}
		}

		return imageUrls;
	}

	/**
	 * 이미지 개수 검증
	 */
	private void validateImageCount(MultipartFile[] files) {
		if (files == null || files.length < 1 || files.length > 8) {
			throw new InvalidImageException("이미지는 1개 이상 8개 이하로 등록해야 합니다");
		}
	}

	/**
	 * 이미지 파일 검증
	 */
	private void validateImageFile(MultipartFile file) {
		if (file.isEmpty()) {
			throw new InvalidImageException("빈 파일은 업로드할 수 없습니다");
		}

		if (file.getSize() > MAX_FILE_SIZE) {
			throw new InvalidImageException("파일 크기는 5MB를 초과할 수 없습니다: " + file.getOriginalFilename());
		}

		String extension = getFileExtension(file.getOriginalFilename());
		if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new InvalidImageException("지원하지 않는 파일 형식입니다: " + extension);
		}
	}

	/**
	 * 업로드 디렉토리 생성
	 */
	private Path createUploadDirectory(Integer classId) {
		LocalDate now = LocalDate.now();
		String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		Path uploadPath = Paths.get(baseDir, datePath, "class-" + classId);

		try {
			Files.createDirectories(uploadPath);
			return uploadPath;
		} catch (IOException e) {
			throw new InvalidImageException("업로드 디렉토리 생성에 실패했습니다");
		}
	}

	/**
	 * 고유한 파일명 생성
	 */
	private String generateFileName(String originalFilename) {
		String extension = getFileExtension(originalFilename);
		String uuid = UUID.randomUUID().toString().substring(0, 8);
		String timestamp = String.valueOf(System.currentTimeMillis());
		return uuid + "-" + timestamp + "." + extension;
	}

	/**
	 * 파일 확장자 추출
	 */
	private String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			throw new InvalidImageException("유효하지 않은 파일명입니다");
		}
		return filename.substring(filename.lastIndexOf('.') + 1);
	}

	/**
	 * 이미지 URL 생성
	 */
	private String buildImageUrl(Integer classId, String fileName) {
		LocalDate now = LocalDate.now();
		String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		return "/uploads/images/" + datePath + "/class-" + classId + "/" + fileName;
	}

	/**
	 * 업로드된 파일 삭제 (롤백)
	 */
	public void deleteUploadedFiles(List<String> imageUrls) {
		for (String url : imageUrls) {
			try {
				Path filePath = Paths.get(baseDir).resolve(url.replace("/uploads/images/", ""));
				Files.deleteIfExists(filePath);
				log.info("로컬 이미지 삭제 완료: {}", url);
			} catch (IOException e) {
				log.warn("로컬 이미지 삭제 실패: {}", url, e);
			}
		}
	}
}
