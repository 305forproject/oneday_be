package com.oneday.core.exception;

import lombok.Getter;

/**
 * 커스텀 예외 기본 클래스
 * 모든 비즈니스 예외는 이 클래스를 상속받아 구현
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}

