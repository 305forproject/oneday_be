package com.oneday.core.service.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.dto.user.UserRequest;
import com.oneday.core.dto.user.UserResponse;
import com.oneday.core.entity.User;
import com.oneday.core.exception.user.DuplicateEmailException;
import com.oneday.core.exception.user.UserNotFoundException;
import com.oneday.core.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 서비스
 * 회원가입, 로그인, 사용자 조회 비즈니스 로직을 처리합니다.
 *
 * @author Zion
 * @since 2025-10-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 회원가입
	 *
	 * @param request 회원가입 요청 정보
	 * @return 생성된 사용자 정보
	 * @throws DuplicateEmailException 이메일 중복 시
	 */
	@Transactional
	public SignUpResponse signUp(SignUpRequest request) {
		log.info("회원가입 시작 - Email: {}", request.email());

		// 이메일 중복 검사
		if (userRepository.findByEmail(request.email()).isPresent()) {
			log.warn("이메일 중복 - Email: {}", request.email());
			throw new DuplicateEmailException();
		}

		// 비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(request.password());

		// 사용자 생성
		User user = User.builder()
			.email(request.email())
			.password(encodedPassword)
			.name(request.name())
			.build();

		User savedUser = userRepository.save(user);

		log.info("회원가입 완료 - UserId: {}, Email: {}", savedUser.getId(), savedUser.getEmail());

		return SignUpResponse.from(savedUser);
	}

	/**
	 * 사용자 생성
	 */
	@Transactional
	public UserResponse createUser(UserRequest request) {
		log.info("사용자 생성 시도: email={}", request.email());

		if (userRepository.existsByEmail(request.email())) {
			log.warn("이메일 중복: email={}", request.email());
			throw new DuplicateEmailException();
		}

		User user = User.builder()
			.email(request.email())
			.password(request.password())
			.name(request.name())
			.build();

		User savedUser = userRepository.save(user);
		log.info("사용자 생성 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

		return UserResponse.from(savedUser);
	}

	/**
	 * ID로 사용자 조회
	 */
	public UserResponse findById(Long userId) {
		log.debug("사용자 조회 시도: userId={}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> {
				log.warn("사용자를 찾을 수 없음: userId={}", userId);
				return new UserNotFoundException();
			});

		return UserResponse.from(user);
	}

	/**
	 * 이메일로 사용자 조회
	 */
	public UserResponse findByEmail(String email) {
		log.debug("사용자 조회 시도: email={}", email);

		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> {
				log.warn("사용자를 찾을 수 없음: email={}", email);
				return new UserNotFoundException();
			});

		return UserResponse.from(user);
	}

	/**
	 * 사용자 삭제
	 */
	@Transactional
	public void deleteUser(Long userId) {
		log.info("사용자 삭제 시도: userId={}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> {
				log.warn("사용자를 찾을 수 없음: userId={}", userId);
				return new UserNotFoundException();
			});

		userRepository.delete(user);
		log.info("사용자 삭제 완료: userId={}", userId);
	}

	/**
	 * 사용자 정보 수정
	 */
	@Transactional
	public void updateUserInfo(Long userId, String name) {
		log.info("사용자 정보 수정 시도: userId={}, name={}", userId, name);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> {
				log.warn("사용자를 찾을 수 없음: userId={}", userId);
				return new UserNotFoundException();
			});

		user.updateInfo(name);
		log.info("사용자 정보 수정 완료: userId={}", userId);
	}
}

