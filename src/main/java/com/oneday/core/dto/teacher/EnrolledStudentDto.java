package com.oneday.core.dto.teacher;

/**
 * 수강생 정보 DTO
 *
 * @param studentId 학생 ID
 * @param studentName 학생 이름
 * @param studentEmail 학생 이메일
 */
public record EnrolledStudentDto(
		long studentId,
		String studentName,
		String studentEmail
) {
}
