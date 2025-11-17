package com.oneday.core.dto.classes;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.oneday.core.entity.Classes;
import com.oneday.core.entity.Images;
import com.oneday.core.entity.Times;

import lombok.Builder;


@Builder
public record ClassDetailResponseDto(
		Integer classId,
		String className,
		String classDetail,
		String curriculum,
		String included,
		String required,
		String location,
		String latitude,
		String longitude,
		String zipcode,
		Integer maxCapacity,
		Integer price,

		// 연관 관계 정보
		String teacherName,
		String categoryName,

		// 1:N 데이터
		List<String> imageUrls,
		List<ScheduleDto> schedules
) {
	/**
	 * 엔티티들을 조합해 DTO Record 생성
	 */
	public static ClassDetailResponseDto of(Classes classes, List<Images> images, List<Times> times) {
		return ClassDetailResponseDto.builder()
				.classId(classes.getClassId())
				.className(classes.getClassName())
				.classDetail(classes.getClassDetail())
				.curriculum(classes.getCurriculum())
				.included(classes.getIncluded())
				.required(classes.getRequired())
				.location(classes.getLocation())
				.latitude(classes.getLatitude())
				.longitude(classes.getLongitude())
				.zipcode(classes.getZipcode())
				.maxCapacity(classes.getMaxCapacity())
				.price(classes.getPrice())

				// 리스트 변환
				.imageUrls(images.stream()
						.map(Images::getImageUrl)
						.toList())
				.schedules(times.stream()
						.map(ScheduleDto::from)
						.toList())
				.build();
	}

	/**
	 * 내부 시간표 정보 Record
	 */
	@Builder
	public record ScheduleDto(
			Integer timeId,
			String startAt,
			String endAt
	) {
		public static ScheduleDto from(Times time) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

			return new ScheduleDto(
					time.getTimeId(),
					time.getStartAt().format(formatter),
					time.getEndAt().format(formatter)
			);
		}
	}
}
