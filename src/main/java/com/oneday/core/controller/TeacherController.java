package com.oneday.core.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneday.core.dto.teacher.EnrolledStudentDto;
import com.oneday.core.dto.teacher.TeacherScheduleResponseDto;
import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.exception.ErrorCode;
import com.oneday.core.service.TeacherService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

	private final TeacherService teacherService;

	/**
	 * 강사 본인의 예정된 스케줄을 조회
	 * (예약 확정 학생 수가 포함된 DTO 리스트 반환)
	 * @param authenticationprincipal 인증된 사용자 정보
	 * @return 예정/지난 스케줄 정보
	 */
	@GetMapping("/my-schedule")
	public ResponseEntity<ApiResponse<TeacherScheduleResponseDto>> getMyTeachingSchedule(
			@AuthenticationPrincipal Long teacherId) {

		if (teacherId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
		}

		log.info("인증된 사용자 ID: {}", teacherId);

		try {
			TeacherScheduleResponseDto scheduleResponse = teacherService.getTeacherSchedule(teacherId);
			return ResponseEntity.ok(ApiResponse.success(scheduleResponse));
		} catch (Exception e) {
			log.error("강사 스케줄 조회 중 오류 발생 (ID: {})", teacherId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}

	/**
	 * 내 스케줄의 특정 수업(timeId)에 등록된 학생 목록을 조회
	 * @param teacherId 조회할 강사의 ID
	 * @param timeId 조회할 수업시간의 ID
	 * @return 등록된 학생 목록
	 */
	@GetMapping("/schedule/{timeId}/students")
	public ResponseEntity<ApiResponse<List<EnrolledStudentDto>>> getEnrolledStudentsForTime(
			@PathVariable int timeId,
			@AuthenticationPrincipal Long teacherId) {

		if (teacherId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
		}

		log.info("인증된 사용자 ID: {}", teacherId);

		try {
			// 서비스 호출 (강사 ID, 시간 ID 전달)
			List<EnrolledStudentDto> students = teacherService.getEnrolledStudents(teacherId, timeId);
			return ResponseEntity.ok(ApiResponse.success(students));
		} catch (Exception e) {
			log.error("수강생 목록 조회 중 오류 발생 (TID: {}, TimeID: {})", teacherId, timeId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}
}
