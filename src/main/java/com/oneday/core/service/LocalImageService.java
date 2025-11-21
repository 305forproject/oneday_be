package com.oneday.core.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oneday.core.exception.classes.InvalidImageException;
import com.oneday.core.util.ImageValidator;

import lombok.RequiredArgsConstructor;
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

	private final ImageValidator imageValidator;
	private final String baseDir;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	/**
	 * 생성자 주입
	 *
	 * @param imageValidator 이미지 검증기
	 * @param baseDir 로컬 저장 기본 디렉토리
	 */
	public LocalImageService(ImageValidator imageValidator,
							 @Value("${file.upload.base-dir}") String baseDir) {
		this.imageValidator = imageValidator;
		this.baseDir = baseDir;
	}

	/**
	 * 이미지 파일 로컬 저장
	 *
	 * @param files 업로드할 파일 배열
	 * @param classId 클래스 ID
	 * @param primaryIndex 대표 이미지 인덱스
	 * @return 저장된 이미지 URL 목록
	 */
	public List<String> uploadImages(MultipartFile[] files, Integer classId, int primaryIndex) {
		imageValidator.validateImageCount(files);

		Path uploadPath = createUploadDirectory(classId);
		List<String> imageUrls = new ArrayList<>();

		for (int i = 0; i < files.length; i++) {
			MultipartFile file = files[i];
			imageValidator.validateImageFile(file);

			String fileName = generateFileName(file.getOriginalFilename());
			Path filePath = uploadPath.resolve(fileName);

			try {
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				String imageUrl = buildImageUrl(classId, fileName);
				imageUrls.add(imageUrl);
				log.info("로컬 이미지 저장 완료: {}, isPrimary={}", imageUrl, i == primaryIndex);
			} catch (IOException e) {
				deleteUploadedFiles(imageUrls);
				throw new InvalidImageException("이미지 저장 중 오류가 발생했습니다: " + file.getOriginalFilename(), e);
			}
		}

		return imageUrls;
	}

	/**
	 * 날짜 기반 경로 생성 (yyyy/MM/dd)
	 *
	 * @return 날짜 경로 문자열
	 */
	private String getDatePath() {
		return LocalDate.now().format(DATE_FORMATTER);
	}

	/**
	 * 업로드 디렉토리 생성
	 */
	private Path createUploadDirectory(Integer classId) {
		String datePath = getDatePath();
		Path uploadPath = Paths.get(baseDir, datePath, "class-" + classId);

		try {
			Files.createDirectories(uploadPath);
			return uploadPath;
		} catch (IOException e) {
			throw new InvalidImageException("업로드 디렉토리 생성에 실패했습니다", e);
		}
	}

	/**
	 * 고유한 파일명 생성
	 */
	private String generateFileName(String originalFilename) {
		String extension = imageValidator.getFileExtension(originalFilename);
		String uuid = UUID.randomUUID().toString().substring(0, 8);
		String timestamp = String.valueOf(System.currentTimeMillis());
		return uuid + "-" + timestamp + "." + extension;
	}


	/**
	 * 이미지 URL 생성
	 */
	private String buildImageUrl(Integer classId, String fileName) {
		String datePath = getDatePath();
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
