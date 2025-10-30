package com.oneday.core.service.user;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * CustomUserDetailsService 테스트
 *
 * @author zionge2k
 * @since 2025-10-30
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
                .email("test@example.com")
                .password("encodedPassword")
                .name("홍길동")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("이메일로 사용자 조회 - 성공")
    void loadUserByUsername_Success() {
        // given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        UserDetails result = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_USER");
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 - 예외 발생")
    void loadUserByUsername_UserNotFound() {
        // given
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다: " + email);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("ADMIN 권한 사용자 조회 - 성공")
    void loadUserByUsername_AdminRole() {
        // given
        User adminUser = User.builder()
                .email("admin@example.com")
                .password("adminPassword")
                .name("관리자")
                .role(Role.ADMIN)
                .build();
        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(adminUser));

        // when
        UserDetails result = customUserDetailsService.loadUserByUsername("admin@example.com");

        // then
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_ADMIN");
    }
}

