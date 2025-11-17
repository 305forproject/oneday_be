package com.oneday.core.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TeacherScheduleDto {

	// Classes
	private Integer classId;
	private String className;
	private String location;
	private String longitude;
	private String latitude;
	private Integer maxCapacity;
	// Times
	private Integer timeId;
	private LocalDateTime startAt;
	private LocalDateTime endAt;
	// Calculate
	@Setter
	private long confirmedStudentCount;
}
