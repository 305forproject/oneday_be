package com.oneday.core.controller;

import java.util.List;

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

import com.oneday.core.config.security.UserPrincipal;
import java.util.List;

import com.oneday.core.dto.classes.ClassDetailResponseDto;
import com.oneday.core.dto.classes.ClassMainResponseDto;
import com.oneday.core.dto.classes.RegisterClassRequest;
import com.oneday.core.dto.classes.RegisterClassResponse;
import com.oneday.core.dto.common.ApiResponse;
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
	 * 클래스 검색
	 *
	 * @param keyword    검색어
	 * @param categoryId 카테고리 ID
	 * @param sort       정렬키
	 * @return 검색 결과
	 * 1. 전체 조회: GET /api/classes
	 * 2. 카테고리 필터: GET /api/classes?categoryId=1
	 * 3. 검색: GET /api/classes?keyword=딸기
	 * 4. 정렬: GET /api/classes?sort=price_asc
	 * 5. 전부 다: GET /api/classes?categoryId=1&keyword=딸기&sort=price_asc
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<ClassMainResponseDto>>> getClasses(
			@RequestParam(required = false) Integer categoryId, // 선택 안 하면 null
			@RequestParam(required = false) String keyword,     // 입력 안 하면 null
			@RequestParam(defaultValue = "latest") String sort  // 기본값 latest
	) {
		try {
			List<ClassMainResponseDto> responseDtos = classService.getClasses(categoryId, keyword, sort);
			return ResponseEntity.ok(ApiResponse.success(responseDtos));

		} catch (Exception e) {
			log.error("클래스 조회 실패: categoryId={}, keyword={}, sort={}", categoryId, keyword, sort, e);
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
	public ResponseEntity<ApiResponse<ClassDetailResponseDto>> getClassById(@PathVariable int classId) {
		try {
			ClassDetailResponseDto classDetail = classService.getClassDetail(classId);
			return ResponseEntity.ok(ApiResponse.success(classDetail));

		} catch (CustomException e) {
			log.warn("클래스 상세 조회 실패 (ID: {}): {}", classId, e.getMessage());
			return ResponseEntity.status(e.getErrorCode().getStatus())
					.body(ApiResponse.error(e.getErrorCode()));
		} catch (Exception e) {
			log.error("클래스 상세 조회 중 오류 (ID: {})", classId, e);
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
	 * @param principal 인증된 사용자 정보 (JWT에서 추출)
	 * @param request   클래스 등록 요청 정보
	 * @return 등록된 클래스 정보
	 */
	@PostMapping
    public ResponseEntity<ApiResponse<RegisterClassResponse>> registerClass(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody RegisterClassRequest request
    ) {
        log.info("클래스 등록 요청: userId={}, className={}", principal.getId(), request.className());

        try {
            RegisterClassResponse response = classService.registerClass(principal.getId(), request);
            log.info("클래스 등록 완료: classId={}, className={}", response.classId(), response.className());

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
        } catch (CustomException e) {
            log.warn("클래스 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(e.getErrorCode()));
        } catch (Exception e) {
            log.error("클래스 등록 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
}
