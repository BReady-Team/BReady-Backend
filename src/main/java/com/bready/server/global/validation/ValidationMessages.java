package com.bready.server.global.validation;

public final class ValidationMessages {

    private ValidationMessages() {}

    public static final String INVALID_NICKNAME =
            "닉네임이 정책을 충족하지 않습니다.";

    public static final String INVALID_EMAIL =
            "이메일 형식이 올바르지 않습니다.";

    public static final String WEAK_PASSWORD =
            "비밀번호가 정책을 충족하지 않습니다.";
}