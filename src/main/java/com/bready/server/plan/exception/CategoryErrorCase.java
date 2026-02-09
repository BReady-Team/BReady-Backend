package com.bready.server.plan.exception;

import com.bready.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCase implements ErrorCase {

    CATEGORY_ACCESS_DENIED(HttpStatus.FORBIDDEN, 4101, "카테고리에 대한 접근 권한이 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, 4102, "존재하지 않는 카테고리입니다."),
    INVALID_SEQUENCE(HttpStatus.BAD_REQUEST, 4103, "sequence 값이 올바르지 않습니다.");

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
