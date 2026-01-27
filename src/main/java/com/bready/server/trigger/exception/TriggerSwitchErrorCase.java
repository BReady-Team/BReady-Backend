package com.bready.server.trigger.exception;

import com.bready.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TriggerSwitchErrorCase implements ErrorCase {

    DECISION_NOT_FOUND(HttpStatus.NOT_FOUND, 7201, "존재하지 않는 결정입니다."),
    KEEP_DECISION_CANNOT_SWITCH(HttpStatus.BAD_REQUEST, 7202, "KEEP 결정에 대해서는 장소 전환이 불가능합니다."),
    ALREADY_SWITCHED(HttpStatus.CONFLICT, 7203, "이미 전환이 완료된 결정입니다."),

    CATEGORY_STATE_NOT_FOUND(HttpStatus.NOT_FOUND, 7204, "카테고리 상태 정보가 없습니다."),
    TO_CANDIDATE_NOT_FOUND(HttpStatus.NOT_FOUND, 7205, "전환 대상 장소 후보가 존재하지 않습니다."),
    TO_CANDIDATE_MISMATCH(HttpStatus.BAD_REQUEST, 7206, "전환 대상 후보가 해당 카테고리에 속하지 않습니다."),
    SAME_CANDIDATE(HttpStatus.CONFLICT, 7207, "현재 대표 후보와 동일한 후보로는 전환할 수 없습니다."),
    FROM_CANDIDATE_NOT_FOUND(HttpStatus.NOT_FOUND, 7208, "현재 대표 장소 후보가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final Integer errorCode;
    private final String message;

    @Override public Integer getHttpStatusCode() { return httpStatus.value(); }
    @Override public Integer getErrorCode() { return errorCode; }
    @Override public String getMessage() { return message; }
}