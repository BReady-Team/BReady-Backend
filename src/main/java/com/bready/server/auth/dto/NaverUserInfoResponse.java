package com.bready.server.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfoResponse {

    private String resultcode;
    private String message;
    private Response response;

    @Getter
    @NoArgsConstructor
    public static class Response {
        private String id;
        private String email;
        private String nickname;
        private String name;
        @JsonProperty("profile_image")
        private String profileImage;
    }
}