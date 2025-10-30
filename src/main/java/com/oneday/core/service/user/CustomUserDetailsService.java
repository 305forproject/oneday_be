package com.oneday.core.service.user;

import com.oneday.core.entity.User;
import com.oneday.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Spring Security 인증을 위한 사용자 정보 조회 서비스
 * 이메일로 사용자를 조회하여 UserDetails 객체로 변환합니다.
 *
 * @author zionge2k
 * @since 2025-10-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 이메일로 사용자 조회
     *
     * @param email 사용자 이메일
     * @return UserDetails 구현체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("사용자 인증 조회: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음: email={}", email);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
                });

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
