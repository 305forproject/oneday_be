package com.oneday.core.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneday.core.dto.auth.LoginRequest;
import com.oneday.core.dto.auth.LoginResponse;
import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.service.auth.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * 회원가입 API
	 * @param request 회원가입 요청 정보
	 * @return 생성된 사용자 정보
	 */
	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
			@Valid @RequestBody SignUpRequest request) {

		log.info("회원가입 API 호출: email={}", request.email());

		SignUpResponse response = authService.signUp(request);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response));
	}

	/**
	 * 로그인 API
	 * @param request 로그인 요청 정보 (이메일, 비밀번호)
	 * @return JWT 토큰 (Access Token, Refresh Token)
	 */
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(
			@Valid @RequestBody LoginRequest request) {

		log.info("로그인 API 호출: email={}", request.email());

		LoginResponse response = authService.login(request);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * JWT 인증 테스트용 헬스체크 API
	 * 인증된 사용자의 이메일을 반환
	 * @param userDetails Spring Security가 자동 주입
	 * @return 인증된 사용자 이메일
	 */
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<String>> getAuthenticatedUser(
			@AuthenticationPrincipal UserDetails userDetails) {

		log.info("인증 확인 API 호출: email={}", userDetails.getUsername());

		return ResponseEntity.ok(
				ApiResponse.success("Authenticated as: " + userDetails.getUsername())
		);
	}
}

