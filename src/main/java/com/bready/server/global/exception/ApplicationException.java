package com.bready.server.global.exception;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorCase errorCase;

    public ApplicationException(ErrorCase errorCase) {
        super(errorCase.getMessage());
        this.errorCase = errorCase;
    }

    public ApplicationException(ErrorCase errorCase, Throwable cause) {
        super(errorCase.getMessage(), cause);
        this.errorCase = errorCase;
    }

    public static ApplicationException from(ErrorCase errorCase) {
        return new ApplicationException(errorCase);
    }
}