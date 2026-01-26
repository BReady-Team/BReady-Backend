package com.bready.server.trigger.exception;

import com.bready.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TriggerErrorCase implements ErrorCase {

    TRIGGER_NOT_FOUND(HttpStatus.NOT_FOUND, 7001, "트리거를 찾을 수 없습니다."),
    TRIGGER_ALREADY_EXECUTED(HttpStatus.BAD_REQUEST, 7002, "이미 실행된 트리거입니다."),
    PLAN_OR_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, 7003, "존재하지 않는 플랜 또는 카테고리 입니다."),
    TRIGGER_FORBIDDEN(HttpStatus.FORBIDDEN, 7004, "트리거를 발생시킬 권한이 없습니다."),
    CATEGORY_STATE_NOT_FOUND(HttpStatus.BAD_REQUEST, 7005, "아직 대표 장소가 선택되지 않은 카테고리입니다.");

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