package com.oneday.core.config.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.oneday.core.exception.auth.ExpiredTokenException;
import com.oneday.core.exception.auth.InvalidTokenException;

/**
 * JwtTokenProvider 테스트
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // 테스트용 JwtProperties 설정
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-that-is-at-least-32-bytes-long-for-hs256");
        jwtProperties.setAccessTokenExpiration(3600000L); // 1시간
        jwtProperties.setRefreshTokenExpiration(604800000L); // 7일

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);

        // 테스트용 사용자 정보
        userDetails = User.builder()
            .username("user@example.com")
            .password("password")
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))
            .build();
    }

    @Test
    @DisplayName("Access Token 생성 - 성공")
    void generateAccessToken_Success() {
        // when
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("Refresh Token 생성 - 성공")
    void generateRefreshToken_Success() {
        // when
        String token = jwtTokenProvider.generateRefreshToken(userDetails);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 - 성공")
    void getEmailFromToken_Success() {
        // given
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // when
        String email = jwtTokenProvider.getEmailFromToken(token);

        // then
        assertThat(email).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 성공")
    void validateToken_Success() {
        // given
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // when & then
        assertThatCode(() -> jwtTokenProvider.validateTokenWithException(token))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 실패 (잘못된 토큰)")
    void validateToken_InvalidToken() {
        // given
        String invalidToken = "invalid.token.here";

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateTokenWithException(invalidToken))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessageContaining("유효하지 않은 토큰");
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 실패 (만료된 토큰)")
    void validateToken_ExpiredToken() throws InterruptedException {
        // given - 만료 시간 1ms로 설정
        jwtProperties.setAccessTokenExpiration(1L);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(jwtProperties);
        String token = expiredProvider.generateAccessToken(userDetails);

        // 토큰 만료 대기
        Thread.sleep(10);

        // when & then
        assertThatThrownBy(() -> expiredProvider.validateTokenWithException(token))
            .isInstanceOf(ExpiredTokenException.class)
            .hasMessageContaining("만료된 토큰");
    }

    @Test
    @DisplayName("토큰에서 Authentication 객체 생성 - 성공")
    void getAuthentication_Success() {
        // given
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // when
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("user@example.com");
        assertThat(authentication.getAuthorities())
            .extracting("authority")
            .contains("ROLE_USER");
    }
}

