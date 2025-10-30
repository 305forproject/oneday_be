package com.oneday.core.config.security;

import com.oneday.core.service.user.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * JWT 인증 필터 테스트
 *
 * @author zionge2k
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("JWT 토큰 인증 - 성공")
    void authenticateWithValidToken() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        String username = "user@example.com";

        request.addHeader("Authorization", "Bearer " + token);

        UserDetails userDetails = mock(UserDetails.class);

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username);
        when(customUserDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(userDetails);

        verify(jwtTokenProvider, times(1)).validateToken(token);
        verify(jwtTokenProvider, times(1)).getUsernameFromToken(token);
        verify(customUserDetailsService, times(1)).loadUserByUsername(username);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("JWT 토큰 인증 - 실패 (토큰 없음)")
    void authenticateWithNoToken() throws ServletException, IOException {
        // given
        // Authorization 헤더 없음

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("JWT 토큰 인증 - 실패 (Bearer 누락)")
    void authenticateWithInvalidPrefix() throws ServletException, IOException {
        // given
        request.addHeader("Authorization", "InvalidPrefix token");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("JWT 토큰 인증 - 실패 (유효하지 않은 토큰)")
    void authenticateWithInvalidToken() throws ServletException, IOException {
        // given
        String token = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtTokenProvider, times(1)).validateToken(token);
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}

