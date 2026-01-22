package com.bready.server.global.validation;

public final class ValidationPatterns {

    private ValidationPatterns() {}

    public static final String NO_WHITESPACE = "^[^\\s]+$";

    public static final String STRONG_PASSWORD =
            "^(?=.*[!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/]).{8,}$";
}