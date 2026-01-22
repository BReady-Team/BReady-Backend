package com.bready.server.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    private Long userId;
    private String nickname;
    private String email;
    private String createdAt;
}
