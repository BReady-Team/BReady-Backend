package com.bready.server.auth.controller;

import com.bready.server.auth.dto.*;
import com.bready.server.auth.service.AuthService;
import com.bready.server.auth.service.KakaoAuthService;
import com.bready.server.auth.service.NaverAuthService;
import com.bready.server.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private KakaoAuthService kakaoAuthService;

    @MockitoBean
    private NaverAuthService naverAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() throws Exception {

        String requestJson = """
            {
              "email": "test@test.com",
              "password": "password123!",
              "nickname": "nickname"
            }
        """;

        SignupResponse response = SignupResponse.builder()
                .userId(1L)
                .nickname("nickname")
                .email("test@test.com")
                .createdAt("2026-03-29")
                .build();

        given(authService.signup(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.nickname").value("nickname"));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {

        String requestJson = """
            {
              "email": "test@test.com",
              "password": "password123!"
            }
        """;

        TokenResponse response = TokenResponse.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .build();

        given(authService.login(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh"));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void refresh_success() throws Exception {

        String requestJson = """
            {
              "refreshToken": "refresh"
            }
        """;

        TokenResponse response = TokenResponse.builder()
                .accessToken("newAccess")
                .refreshToken("newRefresh")
                .build();

        given(authService.refresh(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("newAccess"))
                .andExpect(jsonPath("$.data.refreshToken").value("newRefresh"));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {

        String requestJson = """
            {
              "refreshToken": "refresh"
            }
        """;

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }
}