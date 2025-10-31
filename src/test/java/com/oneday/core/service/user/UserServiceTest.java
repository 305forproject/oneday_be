package com.oneday.core.service.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.dto.user.UserRequest;
import com.oneday.core.dto.user.UserResponse;
import com.oneday.core.entity.Role;
import com.oneday.core.entity.User;
import com.oneday.core.exception.user.DuplicateEmailException;
import com.oneday.core.exception.user.UserNotFoundException;
import com.oneday.core.repository.user.UserRepository;

/**
 * UserService 테스트
 *
 * @author Zion
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	private User user;
	private UserRequest userRequest;
	private SignUpRequest signUpRequest;

	@BeforeEach
	void setUp() {
		user = User.builder()
			.email("user@example.com")
			.password("encodedPassword123")
			.name("홍길동")
			.role(Role.USER)
			.build();

		userRequest = new UserRequest("user@example.com", "password123", "홍길동");

		signUpRequest = new SignUpRequest(
			"user@example.com",
			"password123!",
			"홍길동"
		);
	}

	@Test
	@DisplayName("회원가입 - 성공")
	void signUp_Success() {
		// given
		when(userRepository.findByEmail(signUpRequest.email())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(signUpRequest.password())).thenReturn("encodedPassword");
		when(userRepository.save(any(User.class))).thenReturn(user);

		// when
		SignUpResponse response = userService.signUp(signUpRequest);

		// then
		assertThat(response).isNotNull();
		assertThat(response.email()).isEqualTo(signUpRequest.email());
		assertThat(response.name()).isEqualTo(signUpRequest.name());

		verify(userRepository, times(1)).findByEmail(signUpRequest.email());
		verify(passwordEncoder, times(1)).encode(signUpRequest.password());
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	@DisplayName("회원가입 - 실패 (이메일 중복)")
	void signUp_DuplicateEmail() {
		// given
		when(userRepository.findByEmail(signUpRequest.email())).thenReturn(Optional.of(user));

		// when & then
		assertThatThrownBy(() -> userService.signUp(signUpRequest))
			.isInstanceOf(DuplicateEmailException.class);

		verify(userRepository, times(1)).findByEmail(signUpRequest.email());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("사용자 생성 - 성공")
	void createUser_Success() {
		// given
		when(userRepository.existsByEmail(userRequest.email())).thenReturn(false);
		when(userRepository.save(any(User.class))).thenReturn(user);

		// when
		UserResponse result = userService.createUser(userRequest);

		// then
		assertThat(result).isNotNull();
		assertThat(result.email()).isEqualTo("user@example.com");
		assertThat(result.name()).isEqualTo("홍길동");
		assertThat(result.role()).isEqualTo(Role.USER);

		verify(userRepository, times(1)).existsByEmail(userRequest.email());
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	@DisplayName("사용자 생성 - 실패 (이메일 중복)")
	void createUser_DuplicateEmail() {
		// given
		when(userRepository.existsByEmail(userRequest.email())).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> userService.createUser(userRequest))
			.isInstanceOf(DuplicateEmailException.class)
			.hasMessage("이미 사용 중인 이메일입니다.");

		verify(userRepository, times(1)).existsByEmail(userRequest.email());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("사용자 ID로 조회 - 성공")
	void findById_Success() {
		// given
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// when
		UserResponse result = userService.findById(userId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.email()).isEqualTo("user@example.com");
		assertThat(result.name()).isEqualTo("홍길동");

		verify(userRepository, times(1)).findById(userId);
	}

	@Test
	@DisplayName("사용자 ID로 조회 - 실패 (사용자 없음)")
	void findById_UserNotFound() {
		// given
		Long userId = 999L;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.findById(userId))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage("사용자를 찾을 수 없습니다.");

		verify(userRepository, times(1)).findById(userId);
	}

	@Test
	@DisplayName("이메일로 사용자 조회 - 성공")
	void findByEmail_Success() {
		// given
		String email = "user@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		// when
		UserResponse result = userService.findByEmail(email);

		// then
		assertThat(result).isNotNull();
		assertThat(result.email()).isEqualTo(email);

		verify(userRepository, times(1)).findByEmail(email);
	}

	@Test
	@DisplayName("이메일로 사용자 조회 - 실패 (사용자 없음)")
	void findByEmail_UserNotFound() {
		// given
		String email = "notexist@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.findByEmail(email))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage("사용자를 찾을 수 없습니다.");

		verify(userRepository, times(1)).findByEmail(email);
	}

	@Test
	@DisplayName("사용자 삭제 - 성공")
	void deleteUser_Success() {
		// given
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// when
		userService.deleteUser(userId);

		// then
		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, times(1)).delete(user);
	}

	@Test
	@DisplayName("사용자 삭제 - 실패 (사용자 없음)")
	void deleteUser_UserNotFound() {
		// given
		Long userId = 999L;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.deleteUser(userId))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage("사용자를 찾을 수 없습니다.");

		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, never()).delete(any(User.class));
	}

	@Test
	@DisplayName("사용자 정보 수정 - 성공")
	void updateUserInfo_Success() {
		// given
		Long userId = 1L;
		String newName = "김철수";
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// when
		userService.updateUserInfo(userId, newName);

		// then
		assertThat(user.getName()).isEqualTo(newName);
		verify(userRepository, times(1)).findById(userId);
	}

	@Test
	@DisplayName("관리자 권한 확인 - 일반 사용자")
	void isAdmin_User() {
		// given
		User normalUser = User.builder()
			.email("user@example.com")
			.password("password123")
			.name("일반 사용자")
			.role(Role.USER)
			.build();

		// when
		boolean result = normalUser.isAdmin();

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("관리자 권한 확인 - 관리자")
	void isAdmin_Admin() {
		// given
		User adminUser = User.builder()
			.email("admin@example.com")
			.password("password123")
			.name("관리자")
			.role(Role.ADMIN)
			.build();

		// when
		boolean result = adminUser.isAdmin();

		// then
		assertThat(result).isTrue();
	}
}

