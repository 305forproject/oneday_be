package com.oneday.core.controller.auth;

import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

