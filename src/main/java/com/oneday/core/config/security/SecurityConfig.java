package com.oneday.core.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 설정
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	/**
	 * PasswordEncoder Bean 등록
	 * BCrypt 해시 함수를 사용하여 비밀번호 암호화
	 *
	 * @return BCryptPasswordEncoder
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Security Filter Chain 설정
	 *
	 * @param http HttpSecurity
	 * @return SecurityFilterChain
	 * @throws Exception 설정 중 발생할 수 있는 예외
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// CSRF 비활성화 (JWT 사용)
				.csrf(AbstractHttpConfigurer::disable)

				// 세션 사용하지 않음 (JWT 기반 인증)
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// 요청 권한 설정
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/refresh")
						.permitAll()  // 회원가입, 로그인, 토큰 갱신은 인증 불필요
						.requestMatchers("/api/auth/me")
						.authenticated()
						.anyRequest()
						.authenticated()  // 그 외 요청은 인증 필요
				)

				// JWT 인증 필터 추가
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * AuthenticationManager Bean 등록
	 *
	 * @param config AuthenticationConfiguration
	 * @return AuthenticationManager
	 * @throws Exception 설정 중 발생할 수 있는 예외
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}

