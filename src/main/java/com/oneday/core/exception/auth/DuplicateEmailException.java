package com.oneday.core.exception.auth;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

/**
 * 이메일 중복 예외
 * 회원가입 시 이미 존재하는 이메일로 가입 시도할 때 발생
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public class DuplicateEmailException extends CustomException {

    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }

    public DuplicateEmailException(String message) {
        super(ErrorCode.DUPLICATE_EMAIL, message);
    }
}

