package com.oneday.core.config.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.oneday.core.exception.auth.ExpiredTokenException;
import com.oneday.core.exception.auth.InvalidTokenException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

/**
 * JWT 인증 필터 테스트
 *
 * @author zionge2k
 * @since 2025-01-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 인증 필터 테스트")
class JwtAuthenticationFilterTest {

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private FilterChain filterChain;

	private JwtAuthenticationFilter jwtAuthenticationFilter;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("유효한_토큰으로_인증_성공")
	void authenticateWithValidToken() throws ServletException, IOException {
		// Given
		String token = "valid-jwt-token";
		String email = "test@example.com";

		request.addHeader("Authorization", "Bearer " + token);

		UserDetails userDetails = User.builder()
				.username(email)
				.password("password")
				.authorities(Collections.emptyList())
				.build();

		org.springframework.security.core.Authentication authentication =
				new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
				);

		given(jwtTokenProvider.getAuthentication(token)).willReturn(authentication);
		doNothing().when(jwtTokenProvider).validateToken(token);

		// When
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// Then
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNotNull();
		assertThat(auth.getName()).isEqualTo(email);
		assertThat(auth.isAuthenticated()).isTrue();

		verify(jwtTokenProvider).validateToken(token);
		verify(jwtTokenProvider).getAuthentication(token);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("토큰_없을_때_필터_통과")
	void passFilterWithoutToken() throws ServletException, IOException {
		// Given: Authorization 헤더 없음

		// When
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// Then
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNull();

		verify(filterChain).doFilter(request, response);
		verify(jwtTokenProvider, never()).validateToken(anyString());
	}

	@Test
	@DisplayName("만료된_토큰_인증_실패")
	void failAuthenticationWithExpiredToken() throws ServletException, IOException {
		// Given
		String expiredToken = "expired-jwt-token";
		request.addHeader("Authorization", "Bearer " + expiredToken);

		doThrow(new ExpiredTokenException("만료된 토큰입니다."))
				.when(jwtTokenProvider).validateToken(expiredToken);

		// When
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// Then
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNull();

		verify(jwtTokenProvider).validateToken(expiredToken);
		verify(jwtTokenProvider, never()).getAuthentication(anyString());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("유효하지_않은_토큰_인증_실패")
	void failAuthenticationWithInvalidToken() throws ServletException, IOException {
		// Given
		String invalidToken = "invalid-jwt-token";
		request.addHeader("Authorization", "Bearer " + invalidToken);

		doThrow(new InvalidTokenException("유효하지 않은 토큰입니다."))
				.when(jwtTokenProvider).validateToken(invalidToken);

		// When
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// Then
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNull();

		verify(jwtTokenProvider).validateToken(invalidToken);
		verify(jwtTokenProvider, never()).getAuthentication(anyString());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Bearer_없는_토큰_무시")
	void ignoreTokenWithoutBearer() throws ServletException, IOException {
		// Given
		request.addHeader("Authorization", "just-token-without-bearer");

		// When
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// Then
		verify(jwtTokenProvider, never()).validateToken(anyString());

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNull();

		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("빈_Authorization_헤더_무시")
	void ignoreEmptyAuthorizationHeader() throws ServletException, IOException {
		// Given
		request.addHeader("Authorization", "");

		// When
		jwtAuthenticationFilter.doFilter(request, response, filterChain);

		// Then
		verify(jwtTokenProvider, never()).validateToken(anyString());

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNull();

		verify(filterChain).doFilter(request, response);
	}
}

