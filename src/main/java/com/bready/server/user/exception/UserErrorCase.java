package com.bready.server.user.exception;

import com.bready.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCase implements ErrorCase {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 2001, "사용자를 찾을 수 없습니다."),

    // 회원가입 입력 검증 (명세)
    NICKNAME_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, 4001, "닉네임이 정책을 충족하지 않습니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, 4002, "이메일 형식이 올바르지 않습니다."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, 4003, "비밀번호가 정책을 충족하지 않습니다."),

    // 회원가입 이메일 중복 (명세)
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, 4091, "이미 사용 중인 이메일입니다.");

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
