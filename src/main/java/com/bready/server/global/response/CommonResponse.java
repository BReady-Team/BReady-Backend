package com.bready.server.global.response;

import com.bready.server.global.exception.ErrorCase;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private final Integer errorCode;
    private final String message;
    private final T data;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .message("success")
                .data(data)
                .build();
    }

    public static CommonResponse<?> success() {
        return CommonResponse.builder()
                .message("success")
                .build();
    }

    public static CommonResponse<?> error(ErrorCase errorCase) {
        return CommonResponse.builder()
                .errorCode(errorCase.getErrorCode())
                .message(errorCase.getMessage())
                .build();
    }

    public static CommonResponse<?> error(int errorCode, String message) {
        return CommonResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .build();
    }
}
