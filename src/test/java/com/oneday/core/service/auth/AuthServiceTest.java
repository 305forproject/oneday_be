package com.oneday.core.service.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.oneday.core.config.security.JwtTokenProvider;
import com.oneday.core.dto.auth.LoginRequest;
import com.oneday.core.dto.auth.LoginResponse;
import com.oneday.core.entity.User;
import com.oneday.core.exception.user.InvalidPasswordException;
import com.oneday.core.exception.user.UserNotFoundException;
import com.oneday.core.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @InjectMocks
  private AuthService authService;

  private User user;
  private LoginRequest loginRequest;

  @BeforeEach
  void setUp() {
    user = User.builder()
      .email("user@example.com")
      .password("encodedPassword")
      .name("홍길동")
      .build();

    loginRequest = new LoginRequest("user@example.com", "password123!");
  }

  @Test
  @DisplayName("로그인 - 성공")
  void login_Success() {
    // given
    String accessToken = "generated.access.token";
    String refreshToken = "generated.refresh.token";

    when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginRequest.password(), user.getPassword())).thenReturn(true);
    when(jwtTokenProvider.generateAccessToken(user.getEmail())).thenReturn(accessToken);
    when(jwtTokenProvider.generateRefreshToken(user.getEmail())).thenReturn(refreshToken);

    // when
    LoginResponse response = authService.login(loginRequest);

    // then
    assertThat(response).isNotNull();
    assertThat(response.accessToken()).isEqualTo(accessToken);
    assertThat(response.refreshToken()).isEqualTo(refreshToken);
    assertThat(response.email()).isEqualTo(user.getEmail());
    assertThat(response.name()).isEqualTo(user.getName());

    verify(userRepository, times(1)).findByEmail(loginRequest.email());
    verify(passwordEncoder, times(1)).matches(loginRequest.password(), user.getPassword());
    verify(jwtTokenProvider, times(1)).generateAccessToken(user.getEmail());
    verify(jwtTokenProvider, times(1)).generateRefreshToken(user.getEmail());
  }

  @Test
  @DisplayName("로그인 실패 - 사용자 없음")
  void login_Fail_UserNotFound() {
    // given
    when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> authService.login(loginRequest))
      .isInstanceOf(UserNotFoundException.class)
      .hasMessage("사용자를 찾을 수 없습니다.");

    verify(userRepository, times(1)).findByEmail(loginRequest.email());
    verify(passwordEncoder, never()).matches(any(), any());
    verify(jwtTokenProvider, never()).generateAccessToken(any());
    verify(jwtTokenProvider, never()).generateRefreshToken(any());
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void login_Fail_InvalidPassword() {
    // given
    when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginRequest.password(), user.getPassword())).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> authService.login(loginRequest))
      .isInstanceOf(InvalidPasswordException.class)
      .hasMessage("비밀번호가 일치하지 않습니다.");

    verify(userRepository, times(1)).findByEmail(loginRequest.email());
    verify(passwordEncoder, times(1)).matches(loginRequest.password(), user.getPassword());
    verify(jwtTokenProvider, never()).generateAccessToken(any());
    verify(jwtTokenProvider, never()).generateRefreshToken(any());
  }
}

