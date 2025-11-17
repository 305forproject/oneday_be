package com.oneday.core.config.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

/**
 * 인증된 사용자 정보를 담는 Principal 클래스
 * JWT 토큰에서 추출한 사용자 ID와 Role을 보관하는 경량 객체
 *
 * @author zionge2k
 * @since 2025-01-17
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String role;

    /**
     * JWT 토큰 정보로부터 Principal 생성
     *
     * @param id 사용자 ID
     * @param role 사용자 역할 (예: "USER", "ADMIN")
     */
    public UserPrincipal(Long id, String role) {
        this.id = id;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role)
        );
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
