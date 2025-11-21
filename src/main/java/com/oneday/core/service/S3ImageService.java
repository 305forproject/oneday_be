package com.oneday.core.service;

import java.io.IOException;
import java.io.InputStream;
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
public class S3ImageService {

	private final S3Client s3Client;
	private final ImageValidator imageValidator;
	private final String bucketName;
	private final String region;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	/**
	 * 생성자 주입
	 *
	 * @param s3Client S3 클라이언트
	 * @param imageValidator 이미지 검증기
	 * @param bucketName S3 버킷 이름
	 * @param region S3 리전
	 */
	public S3ImageService(S3Client s3Client,
						  ImageValidator imageValidator,
						  @Value("${aws.s3.bucket}") String bucketName,
						  @Value("${aws.s3.region}") String region) {
		this.s3Client = s3Client;
		this.imageValidator = imageValidator;
		this.bucketName = bucketName;
		this.region = region;
	}

	/**
	 * 이미지 파일 S3 업로드
	 *
	 * @param files 업로드할 파일 배열
	 * @param classId 클래스 ID
	 * @param primaryIndex 대표 이미지 인덱스
	 * @return 저장된 이미지 URL 목록
	 */
	public List<String> uploadImages(MultipartFile[] files, Integer classId, int primaryIndex) {
		imageValidator.validateImageCount(files);

		List<String> imageUrls = new ArrayList<>();

		try {
			for (int i = 0; i < files.length; i++) {
				MultipartFile file = files[i];
				imageValidator.validateImageFile(file);

				String s3Key = generateS3Key(classId, file.getOriginalFilename());
				String imageUrl = uploadToS3(file, s3Key);

				imageUrls.add(imageUrl);
				log.info("S3 이미지 업로드 완료: {}, isPrimary={}", imageUrl, i == primaryIndex);
			}
		} catch (Exception e) {
			// 업로드된 파일들 삭제 (롤백)
			if (!imageUrls.isEmpty()) {
				log.warn("S3 업로드 실패로 인한 롤백 처리: {}개 파일 삭제", imageUrls.size());
				deleteUploadedFiles(imageUrls);
			}
			throw e;
		}

		return imageUrls;
	}

	/**
	 * S3에 파일 업로드
	 */
	private String uploadToS3(MultipartFile file, String s3Key) {
		try (InputStream inputStream = file.getInputStream()) {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(s3Key)
				.contentType(file.getContentType())
				.contentLength(file.getSize())
				.build();

			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

			return buildS3Url(s3Key);
		} catch (IOException e) {
			throw new InvalidImageException("S3 업로드 중 오류가 발생했습니다: " + file.getOriginalFilename(), e);
		}
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
	 * S3 Key 생성 (images/2025/01/27/class-123/uuid-timestamp.jpg)
	 */
	private String generateS3Key(Integer classId, String originalFilename) {
		String datePath = getDatePath();
		String fileName = generateFileName(originalFilename);
		return String.format("images/%s/class-%d/%s", datePath, classId, fileName);
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
	 * S3 URL 생성
	 */
	private String buildS3Url(String s3Key) {
		return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
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
