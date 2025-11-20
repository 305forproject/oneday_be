package com.oneday.core.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime startAt;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime endAt;

	// Calculate
	@Setter
	private long confirmedStudentCount;
}
