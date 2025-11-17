package com.oneday.core.dto.student;

import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@AllArgsConstructor
public class StudentScheduleResponseDto {
	private List<StudentReservationDto> upcomingSchedules; // 예정된 클래스
	private List<StudentReservationDto> pastSchedules;     // 지난 클래스
}
