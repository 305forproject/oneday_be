package com.oneday.core.config.security;

import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;
import com.oneday.core.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CustomUserDetailsService 테스트
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .email("user@example.com")
            .password("encodedPassword")
            .name("홍길동")
            .role(Role.USER)
            .build();
    }

    @Test
    @DisplayName("이메일로 사용자 조회 - 성공")
    void loadUserByUsername_Success() {
        // given
        String email = "user@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        UserDetails result = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("user@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_USER");

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 - 실패 (사용자 없음)")
    void loadUserByUsername_UserNotFound() {
        // given
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다");

        verify(userRepository, times(1)).findByEmail(email);
    }
}

