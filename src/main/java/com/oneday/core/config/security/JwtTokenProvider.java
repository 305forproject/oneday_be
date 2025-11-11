package com.oneday.core.config.security;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.oneday.core.exception.auth.ExpiredTokenException;
import com.oneday.core.exception.auth.InvalidTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 토큰 생성 및 검증을 담당하는 Provider
 * 기존 JwtProperties를 활용하여 설정 관리
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * 비밀키 생성
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     *
     * @param userDetails 사용자 정보
     * @return JWT Access Token
     */
    public String generateAccessToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        List<String> authorities = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        String token = Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("authorities", authorities)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey())
            .compact();

        log.info("Access Token 생성 완료: email={}", userDetails.getUsername());
        return token;
    }

    /**
     * Refresh Token 생성
     *
     * @param userDetails 사용자 정보
     * @return JWT Refresh Token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

        String token = Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey())
            .compact();

        log.info("Refresh Token 생성 완료: email={}", userDetails.getUsername());
        return token;
    }

    /**
     * 토큰에서 이메일 추출
     *
     * @param token JWT 토큰
     * @return 이메일
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 토큰 유효성 검증 (예외 던짐)
     *
     * @param token JWT 토큰
     * @throws InvalidTokenException 잘못된 토큰인 경우
     * @throws ExpiredTokenException 만료된 토큰인 경우
     */
    public void validateTokenWithException(String token) {
        try {
            parseToken(token);
            log.debug("토큰 검증 성공");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            throw new ExpiredTokenException("만료된 토큰입니다.");
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명: {}", e.getMessage());
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰: {}", e.getMessage());
            throw new InvalidTokenException("지원되지 않는 토큰 형식입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있음: {}", e.getMessage());
            throw new InvalidTokenException("토큰이 비어있습니다.");
        }
    }

    /**
     * 토큰에서 Authentication 객체 생성
     *
     * @param token JWT 토큰
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseToken(token);

        List<SimpleGrantedAuthority> grantedAuthorities = extractAuthorities(claims);

        UserDetails userDetails = User.builder()
            .username(claims.getSubject())
            .password("")
            .authorities(grantedAuthorities)
            .build();

        return new UsernamePasswordAuthenticationToken(
            userDetails,
            "",
            grantedAuthorities
        );
    }

    /**
     * Claims에서 권한 정보 추출
     * null 체크 및 타입 검증을 수행하여 안전하게 권한 리스트를 반환합니다.
     *
     * @param claims JWT Claims
     * @return 권한 리스트
     */
    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object authoritiesObj = claims.get("authorities");

        if (authoritiesObj == null) {
            log.debug("토큰에 authorities 클레임 없음 - 빈 권한 리스트 반환");
            return Collections.emptyList();
        }

        if (!(authoritiesObj instanceof List<?>)) {
            log.warn("authorities 클레임 타입 오류: {}", authoritiesObj.getClass());
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>)authoritiesObj;

        return authorities.stream()
            .filter(Objects::nonNull) // null 값 필터링
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }


    /**
     * JWT 토큰에서 사용자 이메일 추출
     *
     * @param token JWT 토큰
     * @return 사용자 이메일
     */
    public String getUserEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * Refresh Token 유효 시간 (초 단위)
     *
     * @return Refresh Token 만료 시간 (초)
     */
    public Long getRefreshTokenExpirationTime() {
        return jwtProperties.getRefreshTokenExpiration() / 1000;
    }

    /**
     * Access Token 유효 시간 (초 단위)
     *
     * @return Access Token 만료 시간 (초)
     */
    public Long getAccessTokenExpirationTime() {
        return jwtProperties.getAccessTokenExpiration() / 1000;
    }

    /**
     * 토큰 유효성 검증 (boolean 반환)
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 파싱
     *
     * @param token JWT 토큰
     * @return Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}

