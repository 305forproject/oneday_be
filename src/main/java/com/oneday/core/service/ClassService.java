package com.oneday.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.oneday.core.dto.classes.ClassMainResponseDto;
import com.oneday.core.entity.Classes;
import com.oneday.core.entity.Images;
import com.oneday.core.exception.ErrorCode;
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

}
