package com.bready.server.auth.controller;

import com.bready.server.auth.dto.SignupRequest;
import com.bready.server.auth.dto.SignupResponse;
import com.bready.server.auth.service.AuthService;
import com.bready.server.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "회원가입",
            description = "이메일/비밀번호로 회원가입을 진행하고 사용자 기본 정보를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 오류 (닉네임/이메일/비밀번호 정책 위반)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이메일 중복",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))
            )
    })
    public CommonResponse<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        return CommonResponse.success(authService.signup(request));
    }
}