package com.bready.server.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class KaKaoLoginRequest {

    @NotBlank(message = "인가 코드는 필수입니다.")
    private String code;

}
