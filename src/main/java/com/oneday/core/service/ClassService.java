package com.oneday.core.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.oneday.core.dto.classes.ClassMainResponseDto;
import com.oneday.core.entity.Classes;
import com.oneday.core.entity.Images;
import com.oneday.core.repository.ClassRepository;
import com.oneday.core.repository.ImageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassService {

	private final ClassRepository classRepository;
	private final ImageRepository imageRepository;

	public Classes getClassById(int classId) {
		return classRepository.findById(classId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
	}

	/**
	 * 모든 클래스 조회
	 *
	 * @return dto 반환
	 */
	public List<ClassMainResponseDto> getAllClasses() {

		// 대표 이미지들 모두 조회
		// (Key: classId, Value: imageUrl)
		Map<Integer, String> representativeImageMap = imageRepository.findAllRepresentativeImages()
				.stream()
				.collect(Collectors.toMap(
						image -> image.getClasses().getClassId(),
						Images::getImageUrl
				));

		// 클래스 목록 조회
		List<Classes> classesList = classRepository.findAllWithTeacherAndCategory();

		// DTO로 변환
		return classesList.stream()
				.map(c -> ClassMainResponseDto.of(
						c,
						representativeImageMap.get(c.getClassId())
				))
				.collect(Collectors.toList());
	}

	/**
	 * 검색
	 * @param keyword 검색어
	 * @param categoryId 카테고리 ID
	 * @param sortKey 정렬 기준
	 * @return dto 반환
	 */
	public List<ClassMainResponseDto> getClasses(Integer categoryId, String keyword, String sortKey) {

		log.info("클래스 검색 시도: keyword={}", keyword);

		// 정렬(Sort) 조건 생성
		Sort sort = Sort.by(Sort.Direction.DESC, "classId"); // 기본: 최신 등록순
		if ("price_asc".equals(sortKey)) {
			sort = Sort.by(Sort.Direction.ASC, "price");
		} else if ("price_desc".equals(sortKey)) {
			sort = Sort.by(Sort.Direction.DESC, "price");
		}
		// 하고 싶은 정렬 있으면 추가하기

		// 검색 조건에 맞는 클래스 목록 조회
		List<Classes> searchResults = classRepository.findAllByConditions(
				categoryId,
				keyword,
				sort
		);

        log.info("클래스 조회 완료: 조회 결과 {}건", searchResults.size());

		if (searchResults.isEmpty()) {
			return Collections.emptyList();
		}

		// 검색된 클래스들의 ID만 추출
		List<Integer> classIds = searchResults.stream()
				.map(Classes::getClassId)
				.collect(Collectors.toList());

		// 해당 ID들의 대표 이미지만 조회
		Map<Integer, String> representativeImageMap = imageRepository.findRepresentativeImagesByClassIds(classIds)
				.stream()
				.collect(Collectors.toMap(
						img -> img.getClasses().getClassId(),
						Images::getImageUrl,
						(existing, replacement) -> existing
				));

		// DTO로 변환
		return searchResults.stream()
				.map(c -> ClassMainResponseDto.of(
						c,
						representativeImageMap.get(c.getClassId())
				))
				.collect(Collectors.toList());
	}
}
