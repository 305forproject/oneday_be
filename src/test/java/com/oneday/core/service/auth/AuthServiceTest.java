package com.oneday.core.service.auth;

import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.entity.User;
import com.oneday.core.exception.auth.DuplicateEmailException;
import com.oneday.core.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * AuthService 단위 테스트
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

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_성공() {
        // Given: 회원가입 요청이 있을 때
        SignUpRequest request = new SignUpRequest(
            "test@example.com",
            "password123",
            "홍길동"
        );

        User savedUser = User.builder()
            .email("test@example.com")
            .password("encodedPassword")
            .name("홍길동")
            .role(com.oneday.core.entity.Role.USER)
            .build();

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // When: 회원가입을 하면
        SignUpResponse response = authService.signUp(request);

        // Then: 사용자가 생성된다
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.name()).isEqualTo("홍길동");

        // 비밀번호 암호화가 호출되었는지 확인
        verify(passwordEncoder).encode("password123");
        // 저장이 호출되었는지 확인
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복 이메일 예외 발생")
    void 중복_이메일_예외_발생() {
        // Given: 이미 가입된 이메일이 있을 때
        SignUpRequest request = new SignUpRequest(
            "test@example.com",
            "password123",
            "홍길동"
        );

        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        // When & Then: 회원가입 시 예외가 발생한다
        assertThatThrownBy(() -> authService.signUp(request))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("이미 사용 중인 이메일입니다");
    }

    @Test
    @DisplayName("비밀번호 암호화 확인")
    void 비밀번호_암호화_확인() {
        // Given: 회원가입 요청이 있을 때
        SignUpRequest request = new SignUpRequest(
            "test@example.com",
            "password123",
            "홍길동"
        );

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("$2a$10$encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation ->
            invocation.getArgument(0)
        );

        // When: 회원가입을 하면
        authService.signUp(request);

        // Then: 비밀번호 암호화가 호출된다
        verify(passwordEncoder).encode("password123");
    }
}

