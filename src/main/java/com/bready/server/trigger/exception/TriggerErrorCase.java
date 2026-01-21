package com.bready.server.trigger.exception;

import com.bready.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TriggerErrorCase implements ErrorCase {

    TRIGGER_NOT_FOUND(HttpStatus.NOT_FOUND, 7001, "트리거를 찾을 수 없습니다."),
    TRIGGER_ALREADY_EXECUTED(HttpStatus.BAD_REQUEST, 7002, "이미 실행된 트리거입니다.");

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