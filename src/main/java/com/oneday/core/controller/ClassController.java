package com.oneday.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.entity.Classes;
import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;
import com.oneday.core.service.ClassService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

	private final ClassService classService;

	/**
	 * 모든 클래스 조회
	 * @return 클래스 목록
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<Classes>>> getAllClasses() {
		try {
			List<Classes> classes = classService.getAllClasses();
			return ResponseEntity.ok(ApiResponse.success(classes));

		} catch (Exception e) {
			log.error("모든 클래스 조회 중 예상치 못한 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}

	/**
	 * 특정 클래스 조회
	 * @param classId 조회할 클래스의 ID
	 * @return 클래스 정보
	 */
	@GetMapping("/{classId}")
	public ResponseEntity<ApiResponse<Classes>> getClassById(@PathVariable int classId) {
		try {
			Classes classInfo = classService.getClassById(classId);
			return ResponseEntity.ok(ApiResponse.success(classInfo));

		} catch (CustomException e) {
			log.warn("클래스 조회 실패 (ID: {}): {}", classId, e.getMessage());
			return ResponseEntity.status(e.getErrorCode().getStatus())
					.body(ApiResponse.error(e.getErrorCode()));
		} catch (Exception e) {
			log.error("클래스 조회 중 예상치 못한 오류 발생 (ID: {})", classId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}
}
