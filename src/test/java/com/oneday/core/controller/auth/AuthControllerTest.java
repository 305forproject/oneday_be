package com.oneday.core.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneday.core.dto.auth.LoginRequest;
import com.oneday.core.dto.auth.LoginResponse;
import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.exception.auth.DuplicateEmailException;
import com.oneday.core.exception.auth.InvalidCredentialsException;
import com.oneday.core.service.auth.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 테스트 (회원가입 + 로그인)
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
    }
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // ============================================
    // Phase 3: 회원가입 테스트
    // ============================================

    @Test
    @DisplayName("회원가입 API 성공")
    void 회원가입_API_성공() throws Exception {
        // Given: 회원가입 요청 데이터
        SignUpRequest request = new SignUpRequest(
            "test@example.com",
            "password123",
            "홍길동"
        );

        SignUpResponse response = new SignUpResponse(
            1L,
            "test@example.com",
            "홍길동",
            LocalDateTime.now()
        );

        given(authService.signUp(any(SignUpRequest.class))).willReturn(response);

        // When & Then: POST /api/auth/signup 호출
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void 회원가입_실패_중복_이메일() throws Exception {
        // Given: 중복 이메일로 회원가입 시도
        SignUpRequest request = new SignUpRequest(
            "duplicate@example.com",
            "password123",
            "홍길동"
        );

        given(authService.signUp(any(SignUpRequest.class)))
            .willThrow(new DuplicateEmailException("이미 사용 중인 이메일입니다"));

        // When & Then: 409 Conflict 응답
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH001"));
    }

    // ============================================
    // Phase 4: 로그인 테스트
    // ============================================

    @Test
    @DisplayName("로그인 API 성공")
    void 로그인_API_성공() throws Exception {
        // Given: 로그인 요청 데이터
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "password123"
        );

        LoginResponse response = new LoginResponse(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh"
        );

        given(authService.login(any(LoginRequest.class))).willReturn(response);

        // When & Then: POST /api/auth/login 호출
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("로그인 실패 - 401 상태 코드 반환")
    void 로그인_실패_401_반환() throws Exception {
        // Given: 잘못된 인증 정보로 로그인 시도
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "wrongpassword"
        );

        given(authService.login(any(LoginRequest.class)))
            .willThrow(new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다"));

        // When & Then: 401 Unauthorized 응답
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH002"));
    }
}

