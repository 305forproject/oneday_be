package com.oneday.core.service.auth;

import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;
import com.oneday.core.exception.auth.DuplicateEmailException;
import com.oneday.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직 처리
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     * @param request 회원가입 요청 정보
     * @return 생성된 사용자 정보
     * @throws DuplicateEmailException 이메일이 이미 존재하는 경우
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        log.info("회원가입 시도: email={}", request.email());

        // 1. 이메일 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            log.warn("중복된 이메일로 회원가입 시도: {}", request.email());
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 3. 사용자 생성 및 저장
        User user = User.builder()
            .email(request.email())
            .password(encodedPassword)
            .name(request.name())
            .role(Role.USER)
            .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        // 4. 응답 반환
        return new SignUpResponse(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getName(),
            savedUser.getCreatedAt()
        );
    }
}

