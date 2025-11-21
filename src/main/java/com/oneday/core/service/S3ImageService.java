package com.oneday.core.service;

import java.io.IOException;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * AWS S3 이미지 업로드 서비스
 *
 * @author zionge2k
 * @since 2025-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageService {

	private final S3Client s3Client;

	@Value("${aws.s3.bucket}")
	private String bucketName;

	@Value("${aws.s3.region}")
	private String region;

	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

	/**
	 * 이미지 파일 S3 업로드
	 *
	 * @param files 업로드할 파일 배열
	 * @param classId 클래스 ID
	 * @param primaryIndex 대표 이미지 인덱스
	 * @return 저장된 이미지 URL 목록
	 */
	public List<String> uploadImages(MultipartFile[] files, Integer classId, int primaryIndex) {
		validateImageCount(files);

		List<String> imageUrls = new ArrayList<>();

		for (int i = 0; i < files.length; i++) {
			MultipartFile file = files[i];
			validateImageFile(file);

			String s3Key = generateS3Key(classId, file.getOriginalFilename());
			String imageUrl = uploadToS3(file, s3Key);

			imageUrls.add(imageUrl);
			log.info("S3 이미지 업로드 완료: {}, isPrimary={}", imageUrl, i == primaryIndex);
		}

		return imageUrls;
	}

	/**
	 * S3에 파일 업로드
	 */
	private String uploadToS3(MultipartFile file, String s3Key) {
		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(s3Key)
				.contentType(file.getContentType())
				.contentLength(file.getSize())
				.build();

			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			return buildS3Url(s3Key);
		} catch (IOException e) {
			throw new InvalidImageException("S3 업로드 중 오류가 발생했습니다: " + file.getOriginalFilename());
		}
	}

	/**
	 * S3 Key 생성 (images/2025/01/27/class-123/uuid-timestamp.jpg)
	 */
	private String generateS3Key(Integer classId, String originalFilename) {
		LocalDate now = LocalDate.now();
		String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		String fileName = generateFileName(originalFilename);
		return String.format("images/%s/class-%d/%s", datePath, classId, fileName);
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
	 * S3 URL 생성
	 */
	private String buildS3Url(String s3Key) {
		return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
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
	 * 파일 확장자 추출
	 */
	private String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			throw new InvalidImageException("유효하지 않은 파일명입니다");
		}
		return filename.substring(filename.lastIndexOf('.') + 1);
	}

	/**
	 * S3에서 파일 삭제
	 */
	public void deleteUploadedFiles(List<String> imageUrls) {
		for (String url : imageUrls) {
			try {
				String s3Key = extractS3KeyFromUrl(url);
				DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
					.bucket(bucketName)
					.key(s3Key)
					.build();

				s3Client.deleteObject(deleteObjectRequest);
				log.info("S3 이미지 삭제 완료: {}", url);
			} catch (Exception e) {
				log.warn("S3 이미지 삭제 실패: {}", url, e);
			}
		}
	}

	/**
	 * S3 URL에서 Key 추출
	 */
	private String extractS3KeyFromUrl(String url) {
		// https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/01/27/class-123/abc.jpg
		// -> images/2025/01/27/class-123/abc.jpg
		String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
		return url.replace(prefix, "");
	}
}
