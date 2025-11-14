package com.oneday.core.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT 설정 프로퍼티
 * application.yml의 jwt 설정을 자동으로 바인딩
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 서명에 사용되는 비밀키
     * 최소 256비트(32자) 이상이어야 함
     */
    private String secret;

    /**
     * Access Token 만료 시간 (밀리초)
     * 기본값: 3600000ms (1시간)
     */
    private long accessTokenExpiration;

    /**
     * Refresh Token 만료 시간 (밀리초)
     * 기본값: 604800000ms (7일)
     */
    private long refreshTokenExpiration;
}

