package com.bready.server.stats.exception;

import com.bready.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StatsErrorCase implements ErrorCase {

    STATS_NOT_FOUND(HttpStatus.NOT_FOUND, 6001, "통계 데이터를 찾을 수 없습니다."),
    STATS_CALCULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6002, "통계 계산에 실패했습니다."),
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, 6003, "period 값이 올바르지 않습니다."),
    INVALID_PARAMETER(HttpStatus.UNAUTHORIZED, 6004, "요청 파라미터가 올바르지 않습니다."),
    INVALID_LIMIT(HttpStatus.CONFLICT, 6005, "limit 값이 올바르지 않습니다.");

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
