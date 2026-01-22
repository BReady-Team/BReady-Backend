package com.bready.server.auth.dto;

import lombok.Getter;

@Getter
public class SignupRequest {
    private String nickname;
    private String email;
    private String password;
}