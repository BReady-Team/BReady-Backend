package com.bready.server.place.exception;

import com.bready.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceErrorCase implements ErrorCase {

    PLACE_SEARCH_FAILED(HttpStatus.BAD_REQUEST, 4001, "장소 검색에 실패했습니다."),
    INVALID_PLACE_TYPE(HttpStatus.BAD_REQUEST, 3002, "장소 타입이 유효하지 않습니다."),
    PLACE_ACCESS_DENIED(HttpStatus.FORBIDDEN, 3003, "해당 장소에 대한 접근 권한이 없습니다.");

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
