package com.oneday.core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.oneday.core.dto.classes.ClassMainResponseDto;
import com.oneday.core.dto.classes.RegisterClassRequest;
import com.oneday.core.dto.classes.RegisterClassResponse;
import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.entity.Classes;
import com.oneday.core.entity.User;
import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;
import com.oneday.core.service.ClassService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

	private final ClassService classService;

	/**
	 * 메인 화면용 모든 클래스 조회 (DTO 반환)
	 *
	 * @return 클래스 목록
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<ClassMainResponseDto>>> getAllClasses() {
		try {
			List<ClassMainResponseDto> responseDtos = classService.getAllClasses();
			return ResponseEntity.ok(ApiResponse.success(responseDtos));

		} catch (Exception e) {
			log.error("모든 클래스 조회 중 예상치 못한 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}

	/**
	 * 특정 클래스 조회
	 *
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

	/**
	 * 클래스 검색
	 *
	 * @param keyword 검색어
	 * @return 검색 결과
	 */
	@GetMapping("/search")
	public ResponseEntity<ApiResponse<List<ClassMainResponseDto>>> searchClasses(
			@RequestParam(required = false, defaultValue = "") String keyword) {

		try {
			List<ClassMainResponseDto> responseDtos;

			if (keyword == null || keyword.trim().isEmpty()) {
				responseDtos = classService.getAllClasses();
			} else {
				responseDtos = classService.searchClasses(keyword);
			}

			return ResponseEntity.ok(ApiResponse.success(responseDtos));

		} catch (Exception e) {
			log.error("클래스 검색 중 오류 발생 (keyword: {})", keyword, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}

	/**
	 * 클래스 등록
	 * <p>
	 * 인증된 사용자가 새로운 클래스를 등록합니다.
	 * 다중 날짜 선택을 지원하며, 동일한 시간대에 여러 날짜를 한 번에 등록할 수 있습니다.
	 * </p>
	 *
	 * @param user    인증된 사용자 (강사)
	 * @param request 클래스 등록 요청 정보
	 * @return 등록된 클래스 정보
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<RegisterClassResponse>> registerClass(
			@AuthenticationPrincipal User user,
			@Valid @RequestBody RegisterClassRequest request
	) {
		log.info("클래스 등록 요청: userId={}, className={}", user.getId(), request.className());

		RegisterClassResponse response = classService.registerClass(user, request);

		log.info("클래스 등록 완료: classId={}, className={}", response.classId(), response.className());

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response));
	}
}
