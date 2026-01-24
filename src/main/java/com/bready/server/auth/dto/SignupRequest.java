package com.bready.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Pattern(
            regexp = "^[^\\s]+$",
            message = "닉네임에 공백을 포함할 수 없습니다."
    )
    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
    private String nickname;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*[@$!%*#?&]).{8,}$",
            message = "비밀번호가 정책을 충족하지 않습니다."
    )
    private String password;
}