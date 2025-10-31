package com.oneday.core.service.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneday.core.config.security.JwtTokenProvider;
import com.oneday.core.dto.auth.LoginRequest;
import com.oneday.core.dto.auth.LoginResponse;
import com.oneday.core.entity.User;
import com.oneday.core.exception.user.InvalidPasswordException;
import com.oneday.core.exception.user.UserNotFoundException;
import com.oneday.core.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증 서비스
 *
 * @author Zion
 * @since 2025-10-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  /**
   * 로그인
   */
  public LoginResponse login(LoginRequest request) {
    log.info("로그인 시도: email={}", request.email());

    User user = userRepository.findByEmail(request.email())
      .orElseThrow(() -> {
        log.warn("사용자를 찾을 수 없음: email={}", request.email());
        throw new UserNotFoundException();
      });

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      log.warn("비밀번호 불일치: email={}", request.email());
      throw new InvalidPasswordException();
    }

    String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

    log.info("로그인 성공: email={}", user.getEmail());

    return new LoginResponse(accessToken, refreshToken, user.getEmail(), user.getName());
  }
}

