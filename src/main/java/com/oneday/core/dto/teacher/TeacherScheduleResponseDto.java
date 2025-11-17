package com.oneday.core.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class TeacherScheduleResponseDto {

	private List<TeacherScheduleDto> upcomingSchedules; // 예정된 클래스 목록
	private List<TeacherScheduleDto> pastSchedules;     // 지난 클래스 목록
}
