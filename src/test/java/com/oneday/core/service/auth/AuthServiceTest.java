package com.oneday.core.service.auth;

import com.oneday.core.config.security.JwtTokenProvider;
import com.oneday.core.dto.auth.LoginRequest;
import com.oneday.core.dto.auth.LoginResponse;
import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;
import com.oneday.core.exception.auth.DuplicateEmailException;
import com.oneday.core.exception.auth.InvalidCredentialsException;
import com.oneday.core.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * AuthService 테스트 (회원가입 + 로그인)
 *
 * @author zionge2k
 * @since 2025-01-26
 */
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

    // ============================================
    // Phase 3: 회원가입 테스트
    // ============================================

    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_성공() {
        // Given: 회원가입 요청이 들어올 때
        SignUpRequest request = new SignUpRequest(
            "test@example.com",
            "password123",
            "홍길동"
        );

        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("$2a$10$encodedPassword");

        User savedUser = User.builder()
            .email("test@example.com")
            .password("$2a$10$encodedPassword")
            .name("홍길동")
            .role(Role.USER)
            .build();

        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // When: 회원가입을 하면
        SignUpResponse response = authService.signUp(request);

        // Then: 사용자가 생성된다
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.name()).isEqualTo("홍길동");

        // 비밀번호가 암호화되었는지 확인
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void 회원가입_실패_중복_이메일() {
        // Given: 이미 존재하는 이메일로 회원가입 시도
        SignUpRequest request = new SignUpRequest(
            "duplicate@example.com",
            "password123",
            "홍길동"
        );

        given(userRepository.existsByEmail("duplicate@example.com")).willReturn(true);

        // When & Then: 예외가 발생한다
        assertThatThrownBy(() -> authService.signUp(request))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("이미 사용 중인 이메일입니다");
    }

    @Test
    @DisplayName("회원가입 시 비밀번호 암호화 확인")
    void 회원가입_시_비밀번호_암호화_확인() {
        // Given: 평문 비밀번호로 회원가입 시도
        SignUpRequest request = new SignUpRequest(
            "test@example.com",
            "plainPassword",
            "홍길동"
        );

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode("plainPassword")).willReturn("$2a$10$encodedPassword");

        User savedUser = User.builder()
            .email("test@example.com")
            .password("$2a$10$encodedPassword")
            .name("홍길동")
            .role(Role.USER)
            .build();

        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // When: 회원가입을 하면
        authService.signUp(request);

        // Then: 비밀번호가 암호화되어 저장된다
        verify(passwordEncoder).encode("plainPassword");
    }

    // ============================================
    // Phase 4: 로그인 테스트
    // ============================================

    @Test
    @DisplayName("로그인 성공")
    void 로그인_성공() {
        // Given: 등록된 사용자가 있을 때
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "password123"
        );

        User user = User.builder()
            .email("test@example.com")
            .password("$2a$10$encodedPassword")
            .name("홍길동")
            .role(com.oneday.core.entity.Role.USER)
            .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(any(UserDetails.class))).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(any(UserDetails.class))).willReturn("refresh-token");

        // When: 로그인을 하면
        LoginResponse response = authService.login(request);

        // Then: 토큰이 반환된다
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        // 비밀번호 검증이 호출되었는지 확인
        verify(passwordEncoder).matches("password123", "$2a$10$encodedPassword");
        // 토큰 생성이 호출되었는지 확인
        verify(jwtTokenProvider).generateAccessToken(any(UserDetails.class));
        verify(jwtTokenProvider).generateRefreshToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void 로그인_실패_존재하지_않는_이메일() {
        // Given: 존재하지 않는 이메일로 로그인 시도
        LoginRequest request = new LoginRequest(
            "notfound@example.com",
            "password123"
        );

        given(userRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // When & Then: 예외가 발생한다
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void 로그인_실패_비밀번호_불일치() {
        // Given: 비밀번호가 일치하지 않을 때
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "wrongpassword"
        );

        User user = User.builder()
            .email("test@example.com")
            .password("$2a$10$encodedPassword")
            .name("홍길동")
            .role(com.oneday.core.entity.Role.USER)
            .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongpassword", "$2a$10$encodedPassword")).willReturn(false);

        // When & Then: 예외가 발생한다
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
    }

    @Test
    @DisplayName("로그인 성공 - UserDetails 변환 확인")
    void 로그인_성공_UserDetails_변환_확인() {
        // Given: 등록된 사용자가 있을 때
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "password123"
        );

        User user = User.builder()
            .email("test@example.com")
            .password("$2a$10$encodedPassword")
            .name("홍길동")
            .role(com.oneday.core.entity.Role.USER)
            .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(any(UserDetails.class))).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(any(UserDetails.class))).willReturn("refresh-token");

        // When: 로그인을 하면
        authService.login(request);

        // Then: User 엔티티가 UserDetails로 변환되어 토큰 생성에 사용된다
        verify(jwtTokenProvider).generateAccessToken(user);
        verify(jwtTokenProvider).generateRefreshToken(user);
    }
}

