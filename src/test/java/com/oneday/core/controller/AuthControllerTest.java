package com.oneday.core.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneday.core.config.security.JwtAuthenticationFilter;
import com.oneday.core.config.security.JwtTokenProvider;
import com.oneday.core.dto.auth.LoginRequest;
import com.oneday.core.dto.auth.LoginResponse;
import com.oneday.core.exception.user.InvalidPasswordException;
import com.oneday.core.exception.user.UserNotFoundException;
import com.oneday.core.service.auth.AuthService;
import com.oneday.core.service.user.UserService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private LoginRequest loginRequest;
  private LoginResponse loginResponse;

  @BeforeEach
  void setUp() {
    loginRequest = new LoginRequest("user@example.com", "password123!");
    loginResponse = new LoginResponse(
      "access.token.here",
      "refresh.token.here",
      "user@example.com",
      "홍길동"
    );
  }

  @Test
  @DisplayName("로그인 API - 성공")
  void login_Success() throws Exception {
    // given
    when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

    // when & then
    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.accessToken").value("access.token.here"))
      .andExpect(jsonPath("$.data.refreshToken").value("refresh.token.here"))
      .andExpect(jsonPath("$.data.email").value("user@example.com"))
      .andExpect(jsonPath("$.data.name").value("홍길동"));

    verify(authService, times(1)).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("로그인 API - 실패 (사용자 없음)")
  void login_Fail_UserNotFound() throws Exception {
    // given
    when(authService.login(any(LoginRequest.class)))
      .thenThrow(new UserNotFoundException());

    // when & then
    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
      .andExpect(status().isNotFound());

    verify(authService, times(1)).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("로그인 API - 실패 (비밀번호 불일치)")
  void login_Fail_InvalidPassword() throws Exception {
    // given
    when(authService.login(any(LoginRequest.class)))
      .thenThrow(new InvalidPasswordException());

    // when & then
    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
      .andExpect(status().isUnauthorized());

    verify(authService, times(1)).login(any(LoginRequest.class));
  }
}

