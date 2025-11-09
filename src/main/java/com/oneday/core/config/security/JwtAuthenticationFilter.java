package com.oneday.core.config.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 인증 필터
 * 모든 HTTP 요청에 대해 JWT 토큰을 검증하고 인증 정보를 설정
 *
 * @author zionge2k
 * @since 2025-01-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		try {
			String token = extractTokenFromRequest(request);

			if (token != null) {
				jwtTokenProvider.validateTokenWithException(token);
				Authentication authentication = jwtTokenProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);

				log.debug("JWT 인증 성공: user={}", authentication.getName());
			}
		} catch (Exception e) {
			log.warn("JWT 인증 실패: {}", e.getMessage());
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Authorization 헤더에서 Bearer 토큰 추출
	 *
	 * @param request HTTP 요청
	 * @return JWT 토큰 (없으면 null)
	 */
	private String extractTokenFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}

		return null;
	}
}

