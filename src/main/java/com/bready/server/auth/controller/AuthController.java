package com.bready.server.auth.controller;

import com.bready.server.auth.dto.*;
import com.bready.server.auth.service.AuthService;
import com.bready.server.auth.service.KakaoAuthService;
import com.bready.server.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoAuthService kakaoAuthService;


    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "회원가입",
            description = "이메일/비밀번호로 회원가입을 진행하고 사용자 기본 정보를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류 (닉네임/이메일/비밀번호 정책 위반)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "409", description = "이메일 중복",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))
            )
    })
    public CommonResponse<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        return CommonResponse.success(authService.signup(request));
    }


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "로그인",
            description = "이메일/비밀번호로 로그인, access/refresh 토큰 발급"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류 (누락 또는 형식 오류)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 실패 (이메일 또는 비밀번호 불일치)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return CommonResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "토큰 재발급",
            description = "refreshToken으로 access/refresh 토큰을 재발급"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "필수 값 누락",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰/만료된 토큰/서버와 불일치",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<TokenResponse> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        return CommonResponse.success(authService.refresh(request));
    }

    @PostMapping("/kakao/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "카카오 로그인",
            description = "인가 코드로 카카오 인증 후 access, refresh 토큰 발급, 사용자 정보 반환"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카카오 로그인 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "이메일 제공 동의 필수",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 카카오 인증 정보",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "502", description = "카카오 인증 서버 통신 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<KakaoLoginResponse> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request
    ) {
        return CommonResponse.success(kakaoAuthService.login(request));
    }
}