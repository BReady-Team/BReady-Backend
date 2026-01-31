package com.bready.server.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoLoginResponse {

    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;
    private UserDto user;

    @Getter
    @Builder
    public static class UserDto {
        private Long userId;
        private String nickname;
        private String email;
        private String joinedAt;
    }
}
