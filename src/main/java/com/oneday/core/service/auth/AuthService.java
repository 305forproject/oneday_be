package com.oneday.core.service.auth;

import com.oneday.core.config.security.JwtTokenProvider;
import com.oneday.core.dto.auth.LoginRequest;
import com.oneday.core.dto.auth.LoginResponse;
import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;
import com.oneday.core.exception.auth.DuplicateEmailException;
import com.oneday.core.exception.auth.InvalidCredentialsException;
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
    private final JwtTokenProvider jwtTokenProvider;

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

    /**
     * 로그인
     * @param request 로그인 요청 정보 (이메일, 비밀번호)
     * @return JWT 토큰 (Access Token, Refresh Token)
     * @throws InvalidCredentialsException 이메일 또는 비밀번호가 올바르지 않은 경우
     */
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도: email={}", request.email());

        // 1. 사용자 조회
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> {
                log.warn("로그인 실패 - 존재하지 않는 이메일: {}", request.email());
                return new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다");
            });

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("로그인 실패 - 비밀번호 불일치: email={}", request.email());
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        // 3. JWT 토큰 생성 (User 엔티티가 UserDetails를 구현하므로 직접 전달)
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("로그인 성공: email={}", request.email());

        // 4. 응답 반환
        return new LoginResponse(accessToken, refreshToken);
    }
}

