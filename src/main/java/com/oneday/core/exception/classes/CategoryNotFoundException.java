package com.oneday.core.exception.classes;

import com.oneday.core.exception.CustomException;
import com.oneday.core.exception.ErrorCode;

/**
 * 카테고리를 찾을 수 없을 때 발생하는 예외
 *
 * @author zionge2k
 * @since 2025-01-26
 */
public class CategoryNotFoundException extends CustomException {

    public CategoryNotFoundException(String message) {
        super(ErrorCode.CATEGORY_NOT_FOUND, message);
    }

    public CategoryNotFoundException() {
        super(ErrorCode.CATEGORY_NOT_FOUND, "존재하지 않는 카테고리입니다");
    }
}
