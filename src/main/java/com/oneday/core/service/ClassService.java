package com.oneday.core.service;

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
	 * 키워드로 클래스 검색
	 * @param keyword 검색어
	 * @return dto 반환
	 */
	public List<ClassMainResponseDto> searchClasses(String keyword) {

		// 키워드로 클래스 목록 조회
		List<Classes> searchResults = classRepository.searchByKeyword(keyword);

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
