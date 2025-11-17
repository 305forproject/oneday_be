package com.oneday.core.exception.classes;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

/**
 * 동일한 강사의 동일한 시간대에 클래스가 이미 존재할 때 발생하는 예외
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public class DuplicateClassTimeException extends CustomException {

    public DuplicateClassTimeException(String message) {
        super(ErrorCode.DUPLICATE_CLASS_TIME, message);
    }

    public DuplicateClassTimeException() {
        super(ErrorCode.DUPLICATE_CLASS_TIME, "이미 등록된 시간대입니다");
    }
}
