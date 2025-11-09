package com.oneday.core.service.auth;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneday.core.config.security.JwtTokenProvider;
import com.oneday.core.dto.auth.LoginRequest;
import com.oneday.core.dto.auth.LoginResponse;
import com.oneday.core.dto.auth.LogoutResponse;
import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.dto.auth.TokenRefreshRequest;
import com.oneday.core.dto.auth.TokenRefreshResponse;
import com.oneday.core.entity.RefreshToken;
import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;
import com.oneday.core.exception.auth.DuplicateEmailException;
import com.oneday.core.exception.auth.InvalidCredentialsException;
import com.oneday.core.exception.auth.InvalidRefreshTokenException;
import com.oneday.core.repository.RefreshTokenRepository;
import com.oneday.core.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	private final RefreshTokenRepository refreshTokenRepository;

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
	@Transactional
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

		// 4. Refresh Token DB에 저장
		saveOrUpdateRefreshToken(user, refreshToken);

		log.info("로그인 성공: email={}", request.email());

		// 5. 응답 반환
		return new LoginResponse(accessToken, refreshToken);
	}

	/**
	 * Refresh Token으로 새로운 Access Token 발급
	 *
	 * @param request Refresh Token 요청
	 * @return 새로운 Access Token과 Refresh Token
	 */
	@Transactional
	public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
		String refreshToken = request.refreshToken();

		// 1. DB에서 Refresh Token 조회
		RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
				.orElseThrow(() -> new InvalidRefreshTokenException(
						"유효하지 않은 Refresh Token입니다"));

		// 2. 만료 여부 확인
		if (savedToken.isExpired()) {
			refreshTokenRepository.delete(savedToken);
			throw new InvalidRefreshTokenException("만료된 Refresh Token입니다");
		}

		// 3. JWT 검증
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			refreshTokenRepository.delete(savedToken);
			throw new InvalidRefreshTokenException("유효하지 않은 Refresh Token입니다");
		}

		// 4. 사용자 이메일 추출
		String email = jwtTokenProvider.getUserEmailFromToken(refreshToken);
		User user = savedToken.getUser();

		// 5. 새로운 토큰 발급
		String newAccessToken = jwtTokenProvider.generateAccessToken(email);
		String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

		// 6. Refresh Token Rotation: DB의 Refresh Token 업데이트
		LocalDateTime newExpiresAt = LocalDateTime.now()
				.plusSeconds(jwtTokenProvider.getRefreshTokenExpirationTime());

		savedToken.update(newRefreshToken, newExpiresAt);
		refreshTokenRepository.save(savedToken);

		log.info("토큰 갱신 완료: email={}", email);

		return new TokenRefreshResponse(
				newAccessToken,
				newRefreshToken,
				jwtTokenProvider.getAccessTokenExpirationTime()
		);
	}

	/**
	 * 로그아웃
	 * Refresh Token을 DB에서 삭제하여 무효화합니다
	 *
	 * @param email 로그아웃할 사용자 이메일
	 * @return 로그아웃 응답
	 */
	@Transactional
	public LogoutResponse logout(String email) {
		log.info("로그아웃 시도: email={}", email);

		// 사용자 조회
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> {
					log.warn("로그아웃 실패 - 존재하지 않는 사용자: {}", email);
					return new InvalidCredentialsException("사용자를 찾을 수 없습니다");
				});

		// Refresh Token 삭제 (멱등성: 없어도 에러 발생하지 않음)
		refreshTokenRepository.deleteByUser(user);

		log.info("로그아웃 완료: email={}", email);

		return LogoutResponse.success();
	}

	/**
	 * Refresh Token 저장 또는 업데이트
	 */
	private void saveOrUpdateRefreshToken(User user, String token) {
		LocalDateTime expiresAt = LocalDateTime.now()
				.plusSeconds(jwtTokenProvider.getRefreshTokenExpirationTime());

		refreshTokenRepository.findByUser(user)
				.ifPresentOrElse(
						existingToken -> {
							existingToken.update(token, expiresAt);
							refreshTokenRepository.save(existingToken);
							log.info("기존 Refresh Token 업데이트: userId={}", user.getId());
						},
						() -> {
							RefreshToken newToken = RefreshToken.builder()
									.token(token)
									.user(user)
									.expiresAt(expiresAt)
									.build();
							refreshTokenRepository.save(newToken);
							log.info("새로운 Refresh Token 저장: userId={}", user.getId());
						}
				);
	}
}

