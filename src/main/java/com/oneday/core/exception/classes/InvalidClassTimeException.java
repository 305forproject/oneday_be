package com.oneday.core.exception.classes;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

/**
 * 유효하지 않은 클래스 시간일 때 발생하는 예외
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public class InvalidClassTimeException extends CustomException {

    public InvalidClassTimeException(String message) {
        super(ErrorCode.INVALID_CLASS_TIME, message);
    }

    public InvalidClassTimeException() {
        super(ErrorCode.INVALID_CLASS_TIME, "유효하지 않은 클래스 시간입니다");
    }
}
