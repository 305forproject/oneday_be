package com.oneday.core.config.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

/**
 * 인증된 사용자 정보를 담는 Principal 클래스
 * JWT 토큰에서 추출한 사용자 ID만 보관하는 경량 객체
 *
 * @author zionge2k
 * @since 2025-01-17
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;

    /**
     * JWT 토큰 정보로부터 Principal 생성
     *
     * @param id 사용자 ID
     */
    public UserPrincipal(Long id) {
        this.id = id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(id);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
