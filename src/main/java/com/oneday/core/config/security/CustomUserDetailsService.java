package com.oneday.core.config.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneday.core.entity.User;
import com.oneday.core.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security의 UserDetailsService 구현체
 * 기존 UserRepository를 활용하여 사용자 조회
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("이메일로 사용자 조회 시도: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("사용자를 찾을 수 없음: {}", email);
                return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
            });

        log.debug("사용자 조회 성공: {}", email);

        return user;
    }
}

