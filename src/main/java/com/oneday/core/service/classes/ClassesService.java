package com.oneday.core.service.classes;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneday.core.dto.classes.ImageDto;
import com.oneday.core.dto.classes.RegisterClassRequest;
import com.oneday.core.dto.classes.RegisterClassResponse;
import com.oneday.core.entity.Categories;
import com.oneday.core.entity.Classes;
import com.oneday.core.entity.Images;
import com.oneday.core.entity.Times;
import com.oneday.core.entity.User;
import com.oneday.core.exception.classes.CategoryNotFoundException;
import com.oneday.core.exception.classes.DuplicateClassTimeException;
import com.oneday.core.exception.classes.InvalidClassTimeException;
import com.oneday.core.exception.classes.InvalidImageException;
import com.oneday.core.repository.CategoriesRepository;
import com.oneday.core.repository.ClassesRepository;
import com.oneday.core.repository.ImageRepository;
import com.oneday.core.repository.TimesRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 클래스 등록 서비스
 * <p>
 * 클래스 등록과 관련된 비즈니스 로직을 처리합니다.
 * </p>
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassesService {

	private final ClassesRepository classesRepository;
	private final CategoriesRepository categoriesRepository;
	private final TimesRepository timesRepository;
	private final ImageRepository imageRepository;

	/**
	 * 클래스 등록
	 * <p>
	 * 1. 카테고리 존재 여부 확인<br/>
	 * 2. 시간 유효성 검증<br/>
	 * 3. 시간 중복 검증 (강사의 모든 클래스 대상)<br/>
	 * 4. 이미지 개수 검증<br/>
	 * 5. 클래스 저장<br/>
	 * 6. 시간 정보 저장 (다중 날짜)<br/>
	 * 7. 이미지 정보 저장 (첫 번째 이미지를 대표 이미지로 설정)
	 * </p>
	 *
	 * @param teacher 강사 (인증된 사용자)
	 * @param request 클래스 등록 요청 정보
	 * @return 등록된 클래스 정보
	 * @throws CategoryNotFoundException      존재하지 않는 카테고리
	 * @throws InvalidClassTimeException      유효하지 않은 시간 정보
	 * @throws DuplicateClassTimeException    시간 중복
	 * @throws InvalidImageException          유효하지 않은 이미지 정보
	 */
	@Transactional
	public RegisterClassResponse registerClass(User teacher, RegisterClassRequest request) {
		log.info("클래스 등록 시작: teacherId={}, className={}", teacher.getId(), request.className());

		// 1. 카테고리 검증
		Categories category = validateCategory(request.category().getKoreanName());

		// 2. 시간 유효성 검증
		validateTime(request.startTime(), request.endTime());

		// 3. 시간 중복 검증 (모든 날짜에 대해)
		validateTimeOverlap(teacher.getId(), request.dates(), request.startTime(), request.endTime());

		// 4. 이미지 검증
		validateImages(request.images());

		// 5. 클래스 저장
		Classes savedClass = saveClass(teacher, category, request);

		// 6. 시간 정보 저장 (다중 날짜)
		saveTimes(savedClass, request.dates(), request.startTime(), request.endTime());

		// 7. 이미지 정보 저장
		saveImages(savedClass, request.images());

		log.info("클래스 등록 완료: classId={}, className={}", savedClass.getClassId(), savedClass.getClassName());

		return new RegisterClassResponse(
				savedClass.getClassId(),
				savedClass.getClassName(),
				category.getCategory()
		);
	}

	/**
	 * 카테고리 존재 여부 확인
	 * <p>
	 * CategoryType Enum의 한글명으로 Categories 테이블에서 조회합니다.
	 * </p>
	 */
	private Categories validateCategory(String categoryName) {
		return categoriesRepository.findByCategory(categoryName)
				.orElseThrow(() -> {
					log.warn("존재하지 않는 카테고리: categoryName={}", categoryName);
					return new CategoryNotFoundException("존재하지 않는 카테고리입니다: " + categoryName);
				});
	}

	/**
	 * 시간 유효성 검증
	 * <p>
	 * 시작 시간이 종료 시간보다 이른지 확인합니다.
	 * </p>
	 */
	private void validateTime(java.time.LocalTime startTime, java.time.LocalTime endTime) {
		if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
			log.warn("유효하지 않은 시간 정보: startTime={}, endTime={}", startTime, endTime);
			throw new InvalidClassTimeException("시작 시간은 종료 시간보다 이전이어야 합니다");
		}
	}

	/**
	 * 시간 중복 검증 (다중 날짜)
	 * <p>
	 * 강사의 기존 클래스와 시간이 겹치는지 확인합니다.
	 * 모든 날짜에 대해 중복을 검사합니다.
	 * </p>
	 */
	private void validateTimeOverlap(Long teacherId, List<java.time.LocalDate> dates,
									  java.time.LocalTime startTime, java.time.LocalTime endTime) {
		for (java.time.LocalDate date : dates) {
			LocalDateTime startAt = LocalDateTime.of(date, startTime);
			LocalDateTime endAt = LocalDateTime.of(date, endTime);

			boolean isOverlapping = timesRepository.existsOverlappingTimesByTeacher(
					teacherId,
					startAt,
					endAt
			);

			if (isOverlapping) {
				log.warn("시간 중복 발생: teacherId={}, date={}, startTime={}, endTime={}",
						teacherId, date, startTime, endTime);
				throw new DuplicateClassTimeException(
						String.format("이미 등록된 시간대입니다: %s %s ~ %s", date, startTime, endTime)
				);
			}
		}
	}

	/**
	 * 이미지 검증
	 * <p>
	 * 이미지 개수가 1~5개 범위인지 확인합니다.
	 * </p>
	 */
	private void validateImages(List<ImageDto> images) {
		if (images.size() < 1 || images.size() > 5) {
			log.warn("유효하지 않은 이미지 개수: size={}", images.size());
			throw new InvalidImageException("이미지는 1개 이상 5개 이하로 등록해야 합니다");
		}
	}

	/**
	 * 클래스 엔티티 저장
	 */
	private Classes saveClass(User teacher, Categories category, RegisterClassRequest request) {
		Classes classes = Classes.builder()
				.teacher(teacher)
				.category(category)
				.className(request.className())
				.classDetail(request.classDetail())
				.curriculum(request.curriculum())
				.included(request.included())
				.required(request.required())
				.location(request.location())
				.longitude(request.longitude())
				.latitude(request.latitude())
				.zipcode(request.zipcode())
				.maxCapacity(request.maxCapacity())
				.price(request.price())
				.build();

		return classesRepository.save(classes);
	}

	/**
	 * 시간 정보 저장 (다중 날짜)
	 * <p>
	 * 선택된 모든 날짜에 대해 Times 엔티티를 생성합니다.
	 * </p>
	 */
	private void saveTimes(Classes classes, List<java.time.LocalDate> dates,
						   java.time.LocalTime startTime, java.time.LocalTime endTime) {
		List<Times> timeEntities = dates.stream()
				.map(date -> Times.builder()
						.classes(classes)
						.startAt(LocalDateTime.of(date, startTime))
						.endAt(LocalDateTime.of(date, endTime))
						.build())
				.toList();

		timesRepository.saveAll(timeEntities);
		log.info("시간 정보 저장 완료: classId={}, count={}", classes.getClassId(), timeEntities.size());
	}

	/**
	 * 이미지 정보 저장
	 * <p>
	 * 첫 번째 이미지를 대표 이미지로 설정합니다.
	 * </p>
	 */
	private void saveImages(Classes classes, List<ImageDto> images) {
		List<Images> imageEntities = images.stream()
				.map(imageDto -> Images.builder()
						.classes(classes)
						.imageUrl(imageDto.imageUrl())
						.isRepresentative(images.indexOf(imageDto) == 0)
						.build())
				.toList();

		imageRepository.saveAll(imageEntities);
		log.info("이미지 정보 저장 완료: classId={}, count={}", classes.getClassId(), imageEntities.size());
	}
}
