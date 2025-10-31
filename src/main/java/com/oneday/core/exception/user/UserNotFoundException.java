package com.oneday.core.exception.user;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 *
 * @author Zion
 * @since 2025-10-30
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("사용자를 찾을 수 없습니다.");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}

