package com.oneday.core.dto.classes;

import com.oneday.core.entity.Classes;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassMainResponseDto {
	private int classId;
	private String className;
	private String teacherName;
	private int price;
	private String representativeImageUrl;
	private String location;
	private String categoryName;

	/**
	 * Classes 엔티티와 대표 이미지 URL을 받아 DTO를 생성
	 */
	public static ClassMainResponseDto of(Classes entity, String imageUrl) {

		return ClassMainResponseDto.builder()
				.classId(entity.getClassId())
				.className(entity.getClassName())
				.teacherName(entity.getTeacher().getName())
				.price(entity.getPrice())
				.representativeImageUrl(imageUrl)
				.location(entity.getLocation())
				.categoryName(entity.getCategory().getCategory())
				.build();
	}
}
