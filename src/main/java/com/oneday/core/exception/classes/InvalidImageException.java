package com.oneday.core.exception.classes;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

/**
 * 유효하지 않은 이미지일 때 발생하는 예외
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public class InvalidImageException extends CustomException {

    public InvalidImageException(String message) {
        super(ErrorCode.INVALID_IMAGE, message);
    }

    public InvalidImageException(String message, Throwable cause) {
        super(ErrorCode.INVALID_IMAGE, message, cause);
    }

    public InvalidImageException() {
        super(ErrorCode.INVALID_IMAGE, "유효하지 않은 이미지입니다");
    }
}
