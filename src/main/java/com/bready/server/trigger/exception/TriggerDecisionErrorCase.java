package com.bready.server.trigger.exception;

import com.bready.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TriggerDecisionErrorCase implements ErrorCase {

    TRIGGER_NOT_FOUND(HttpStatus.NOT_FOUND, 7101, "존재하지 않는 트리거입니다."),
    DECISION_ALREADY_MADE(HttpStatus.CONFLICT, 7102, "이미 결정이 완료된 트리거입니다.");

    private final HttpStatus httpStatus;
    private final Integer errorCode;
    private final String message;

    @Override
    public Integer getHttpStatusCode() { return httpStatus.value(); }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
