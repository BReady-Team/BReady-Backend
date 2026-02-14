package com.bready.server.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NaverLoginResponse {

    private String accessToken;
    private String refreshToken;
    @JsonProperty("isNewUser")
    private boolean newUser;
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