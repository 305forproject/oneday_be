package com.oneday.core.exception.user;

/**
 * 이메일이 중복될 때 발생하는 예외
 *
 * @author Zion
 * @since 2025-10-30
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("이미 사용 중인 이메일입니다.");
    }

    public DuplicateEmailException(String message) {
        super(message);
    }
}

