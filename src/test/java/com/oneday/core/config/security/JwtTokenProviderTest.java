package com.oneday.core.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * JWT 토큰 제공자 테스트
 *
 * @author zionge2k
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private final String testUsername = "test@example.com";
    private final String testSecret = "testSecretKeyForJwtTokenProviderTestMustBeLongerThan256Bits";
    private final long testAccessTokenExpiration = 3600000L; // 1시간
    private final long testRefreshTokenExpiration = 604800000L; // 7일

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getSecret()).thenReturn(testSecret);
        lenient().when(jwtProperties.getAccessTokenExpiration()).thenReturn(testAccessTokenExpiration);
        lenient().when(jwtProperties.getRefreshTokenExpiration()).thenReturn(testRefreshTokenExpiration);
    }

    @Test
    @DisplayName("Access Token 생성 성공")
    void 토큰_생성_성공() {
        // given
        String username = testUsername;

        // when
        String token = jwtTokenProvider.generateAccessToken(username);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 header.payload.signature 구조
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void 유효한_토큰_검증_성공() {
        // given
        String token = jwtTokenProvider.generateAccessToken(testUsername);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰에서 사용자명 추출 성공")
    void 토큰에서_사용자명_추출_성공() {
        // given
        String token = jwtTokenProvider.generateAccessToken(testUsername);

        // when
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // then
        assertThat(username).isEqualTo(testUsername);
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void 만료된_토큰_검증_실패() {
        // given
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(1L); // 1ms로 설정
        String token = jwtTokenProvider.generateAccessToken(testUsername);

        // when
        try {
            Thread.sleep(10); // 토큰 만료 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // then
        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("잘못된 토큰 검증 실패")
    void 잘못된_토큰_검증_실패() {
        // given
        String invalidToken = "invalid.token.format";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Refresh Token 생성 성공")
    void 리프레시_토큰_생성_성공() {
        // given
        String username = testUsername;

        // when
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        // then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("토큰 만료일 추출 성공")
    void 토큰_만료일_추출_성공() {
        // given
        String token = jwtTokenProvider.generateAccessToken(testUsername);
        Date now = new Date();

        // when
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // then
        assertThat(expirationDate).isAfter(now);
        assertThat(expirationDate.getTime()).isCloseTo(
            now.getTime() + testAccessTokenExpiration,
            within(1000L) // 1초 오차 허용
        );
    }

    @Test
    @DisplayName("null 토큰 검증 시 false 반환")
    void null_토큰_검증_실패() {
        // when
        boolean isValid = jwtTokenProvider.validateToken(null);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 토큰 검증 시 false 반환")
    void 빈_문자열_토큰_검증_실패() {
        // when
        boolean isValid = jwtTokenProvider.validateToken("");

        // then
        assertThat(isValid).isFalse();
    }
}

