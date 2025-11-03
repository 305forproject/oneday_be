package com.oneday.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드 정의
 * HTTP 상태 코드, 내부 에러 코드, 에러 메시지를 관리
 *
 * @author zionge2k
 * @since 2025-01-26
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 에러 (400번대)
    INVALID_INPUT(400, "COMMON001", "입력값이 올바르지 않습니다"),
    UNAUTHORIZED(401, "COMMON002", "인증이 필요합니다"),
    FORBIDDEN(403, "COMMON003", "접근 권한이 없습니다"),
    NOT_FOUND(404, "COMMON004", "요청한 리소스를 찾을 수 없습니다"),
    METHOD_NOT_ALLOWED(405, "COMMON005", "허용되지 않은 HTTP 메서드입니다"),
    CONFLICT(409, "COMMON006", "리소스 충돌이 발생했습니다"),

    // 서버 에러 (500번대)
    INTERNAL_SERVER_ERROR(500, "COMMON999", "서버 내부 오류가 발생했습니다"),

    // 인증/인가 관련 에러
    DUPLICATE_EMAIL(409, "AUTH001", "이미 사용 중인 이메일입니다"),
    INVALID_CREDENTIALS(401, "AUTH002", "이메일 또는 비밀번호가 올바르지 않습니다"),
    INVALID_TOKEN(401, "AUTH003", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(401, "AUTH004", "만료된 토큰입니다"),
    USER_NOT_FOUND(404, "AUTH005", "사용자를 찾을 수 없습니다");

    private final int status;
    private final String code;
    private final String message;
}

