package com.bready.server.auth.exception;

import com.bready.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCase implements ErrorCase {

    // 토큰 관련
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 1001, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, 1002, "토큰이 만료되었습니다."),

    // 권한
    UNAUTHORIZED(HttpStatus.FORBIDDEN, 1003, "접근 권한이 없습니다."),

    // 로그인
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, 4011, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // refresh 전용
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 4012, "리프레시 토큰이 유효하지 않습니다.");


    private final HttpStatus httpStatus;
    private final Integer errorCode;
    private final String message;

    @Override
    public Integer getHttpStatusCode() {
        return httpStatus.value();
    }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}