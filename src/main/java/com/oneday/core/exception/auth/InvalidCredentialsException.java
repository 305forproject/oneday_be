package com.oneday.core.exception.auth;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

/**
 * 잘못된 자격증명 예외
 * 로그인 시 이메일 또는 비밀번호가 틀렸을 때 발생
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public class InvalidCredentialsException extends CustomException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }

    public InvalidCredentialsException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, message);
    }
}

