package com.oneday.core.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oneday.core.enums.StorageType;

import lombok.extern.slf4j.Slf4j;

/**
 * 이미지 파일 업로드 서비스 (Facade)
 * storage-type에 따라 로컬 또는 S3 저장 방식을 선택합니다.
 *
 * <p>개발자가 직접 저장 방식을 선택하려면:
 * <pre>
 * // S3 사용
 * s3ImageService.uploadImages(files, classId, primaryIndex);
 *
 * // 로컬 사용
 * localImageService.uploadImages(files, classId, primaryIndex);
 * </pre>
 *
 * @author zionge2k
 * @since 2025-01-27
 */
@Slf4j
@Service
public class ImageService {

	private final S3ImageService s3ImageService;
	private final LocalImageService localImageService;
	private final StorageType storageType;

	/**
	 * 생성자 주입
	 *
	 * @param s3ImageService S3 이미지 서비스
	 * @param localImageService 로컬 이미지 서비스
	 * @param storageType 저장소 타입 설정값
	 */
	public ImageService(S3ImageService s3ImageService,
						LocalImageService localImageService,
						@Value("${file.upload.storage-type}") String storageType) {
		this.s3ImageService = s3ImageService;
		this.localImageService = localImageService;
		this.storageType = StorageType.from(storageType);
	}

	/**
	 * 이미지 파일 업로드 및 저장
	 * application.yml의 storage-type 설정에 따라 자동으로 저장소를 선택합니다.
	 *
	 * @param files 업로드할 파일 배열
	 * @param classId 클래스 ID
	 * @param primaryIndex 대표 이미지 인덱스
	 * @return 저장된 이미지 URL 목록
	 */
	public List<String> uploadImages(MultipartFile[] files, Integer classId, int primaryIndex) {
		log.info("이미지 업로드 시작: storage-type={}, classId={}, fileCount={}", storageType, classId, files.length);

		return switch (storageType) {
			case S3 -> s3ImageService.uploadImages(files, classId, primaryIndex);
			case LOCAL -> localImageService.uploadImages(files, classId, primaryIndex);
		};
	}

	/**
	 * 업로드된 파일 삭제 (롤백)
	 * application.yml의 storage-type 설정에 따라 자동으로 저장소를 선택합니다.
	 *
	 * @param imageUrls 삭제할 이미지 URL 목록
	 */
	public void deleteUploadedFiles(List<String> imageUrls) {
		log.info("이미지 삭제 시작: storage-type={}, urlCount={}", storageType, imageUrls.size());

		switch (storageType) {
			case S3 -> s3ImageService.deleteUploadedFiles(imageUrls);
			case LOCAL -> localImageService.deleteUploadedFiles(imageUrls);
		}
	}
}
