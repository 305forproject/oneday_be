package com.oneday.core.dto.student;

import lombok.Getter;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StudentReservationDto {

	// Classes
	private Integer classId;
	private String className;
	private String location;
	private String longitude;
	private String latitude;
	private Integer price;

	// Times
	private Integer timeId;
	private LocalDateTime startAt;
	private LocalDateTime endAt;

	// Teacher (User)
	private String teacherName;
	private String teacherEmail;

	// ReservationStatus
	private String statusName;
}
