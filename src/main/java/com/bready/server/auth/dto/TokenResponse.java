package com.bready.server.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    // accessToken, refreshToken 필드로 받기 (로그인 하면 발급 되는 토큰들)
}
