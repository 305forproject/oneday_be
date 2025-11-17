package com.oneday.core.config.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
     * userId, role 클레임 추가
     *
     * @param userDetails 사용자 정보
     * @return JWT Access Token
     */
    public String generateAccessToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        // User 엔티티에서 userId, role 추출
        Long userId = null;
        String role = null;
        if (userDetails instanceof com.oneday.core.entity.User) {
            com.oneday.core.entity.User user = (com.oneday.core.entity.User) userDetails;
            userId = user.getId();
            role = user.getRole().name();
        }

        if (userId == null) {
            log.warn("Access Token 생성 실패: userId가 null입니다. email={}", userDetails.getUsername());
            throw new InvalidTokenException("userId가 null이므로 Access Token을 생성할 수 없습니다.");
        }
        String token = Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("userId", userId)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey())
            .compact();

        log.info("Access Token 생성 완료: email={}, userId={}, role={}",
                 userDetails.getUsername(), userId, role);
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

        // User 엔티티에서 userId 추출
        Long userId = null;
        if (userDetails instanceof com.oneday.core.entity.User) {
            userId = ((com.oneday.core.entity.User) userDetails).getId();
        }

        String token = Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("userId", userId)  // userId 클레임 추가
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey())
            .compact();

        log.info("Refresh Token 생성 완료: email={}, userId={}", userDetails.getUsername(), userId);
        return token;
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
     * UserPrincipal을 Principal로 사용
     *
     * @param token JWT 토큰
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        // 토큰에서 userId, role 추출
        Long userId = getUserIdFromToken(token);
        String roleStr = getRoleFromToken(token);

        // UserPrincipal 생성 (ID, role 포함)
        UserPrincipal principal = new UserPrincipal(userId, roleStr);

        return new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities() // UserPrincipal의 권한 사용
        );
    }



    /**
     * JWT 토큰에서 Role 추출
     *
     * @param token JWT 토큰
     * @return Role 문자열 (없으면 null)
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        Object roleClaim = claims.get("role");

        if (roleClaim == null) {
            log.debug("토큰에 role 클레임이 없음");
            return null;
        }

        return roleClaim.toString();
    }

    /**
     * JWT 토큰에서 User ID 추출
     *
     * @param token JWT 토큰
     * @return User ID (없으면 null)
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object userIdClaim = claims.get("userId");

        if (userIdClaim == null) {
            log.debug("토큰에 userId 클레임이 없음");
            return null;
        }

        // JJWT 라이브러리는 작은 숫자를 Integer로 파싱할 수 있음
        // User ID가 Integer 범위 내일 때 Integer로 반환되므로 방어 코드 필요
        if (userIdClaim instanceof Integer) {
            return ((Integer) userIdClaim).longValue();
        } else if (userIdClaim instanceof Long) {
            return (Long) userIdClaim;
        } else {
            log.warn("userId 클레임 타입 오류: {}", userIdClaim.getClass());
            return null;
        }
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

