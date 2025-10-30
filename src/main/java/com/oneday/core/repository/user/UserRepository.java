package com.oneday.core.repository.user;

import com.oneday.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 리포지토리
 *
 * @author Zion
 * @since 2025-10-30
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 중복 확인
     */
    boolean existsByEmail(String email);
}

