package com.oneday.core.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class EnrolledStudentDto {

	private long studentId;
	private String studentName;
	private String studentEmail;
}
