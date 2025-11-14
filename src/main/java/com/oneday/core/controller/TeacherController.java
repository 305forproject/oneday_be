package com.oneday.core.controller;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneday.core.dto.EnrolledStudentDto;
import com.oneday.core.dto.TeacherScheduleResponseDto;
import com.oneday.core.service.TeacherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

	private static final Logger log = LoggerFactory.getLogger(TeacherController.class);
	private final TeacherService teacherService;

	/**
	 * 강사 본인의 예정된 스케줄을 조회
	 * (예약 확정 학생 수가 포함된 DTO 리스트 반환)
	 */
	@GetMapping("/my-schedule")
	public ResponseEntity<?> getMyTeachingSchedule(HttpSession session) {

		// 세션에서 강사 ID 조회 (long 타입)
		// 추후 토큰으로 수정 예정
		if (session == null || session.getAttribute("userId") == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다. (강사)"));
		}

		long teacherId = (Long)session.getAttribute("userId");

		try {
			TeacherScheduleResponseDto scheduleResponse = teacherService.getTeacherSchedule(teacherId);
			return ResponseEntity.ok(scheduleResponse);

		} catch (Exception e) {
			log.error("강사 스케줄 조회 중 오류 발생 (ID: {})", teacherId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "스케줄 조회 중 문제가 발생했습니다."));
		}
	}

	/**
	 * 내 스케줄의 특정 수업(timeId)에 등록된 학생 목록을 조회
	 */
	@GetMapping("/schedule/{timeId}/students")
	public ResponseEntity<?> getEnrolledStudentsForTime(
			@PathVariable int timeId,
			HttpSession session) {

		// 토큰에서 가져오게 추후 수정 예정
		if (session == null || session.getAttribute("userId") == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "로그인이 필요합니다. (강사)"));
		}

		long teacherId = (Long)session.getAttribute("userId");

		try {
			// 서비스 호출 (강사 ID, 시간 ID 전달)
			List<EnrolledStudentDto> students = teacherService.getEnrolledStudents(teacherId, timeId);
			return ResponseEntity.ok(students);

		} catch (Exception e) {
			log.error("수강생 목록 조회 중 오류 발생 (TID: {}, TimeID: {})", teacherId, timeId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "수강생 조회 중 문제가 발생했습니다."));
		}
	}
}
