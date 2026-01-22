package com.bready.server.user.domain;

public enum UserStatus {
    ACTIVE,            // 정상 사용자, 기본 값
    INACTIVE,          // 비활성 사용자 (휴면 계정..)
    WITHDRAWN,         // 탈퇴한 사용자
}
