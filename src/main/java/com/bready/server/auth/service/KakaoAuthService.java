package com.bready.server.auth.service;

import com.bready.server.auth.client.KakaoOAuthClient;
import com.bready.server.auth.client.KakaoUserInfoClient;
import com.bready.server.auth.dto.*;
import com.bready.server.auth.exception.AuthErrorCase;
import com.bready.server.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoUserInfoClient kakaoUserInfoClient;

    private final KakaoAuthTransactionHandler kakaoAuthTransactionHandler;

    public KakaoLoginResponse login(KakaoLoginRequest request) {

        String code = request.getCode();
        if (code == null || code.isBlank()) {
            throw new ApplicationException(AuthErrorCase.INVALID_KAKAO_AUTH);
        }

        // 카카오 토큰 교환 (트랜잭션 외부)
        KakaoTokenResponse token = exchangeKakaoToken(code);
        String kakaoAccessToken = token.getAccessToken();
        if (kakaoAccessToken == null || kakaoAccessToken.isBlank()) {
            throw new ApplicationException(AuthErrorCase.INVALID_KAKAO_AUTH);
        }

        // 사용자 정보 조회 (트랜잭션 외부)
        KakaoUserInfoResponse userInfo = fetchKakaoUserInfo(kakaoAccessToken);
        if (userInfo.getId() == null) {
            throw new ApplicationException(AuthErrorCase.INVALID_KAKAO_AUTH);
        }

        return kakaoAuthTransactionHandler.processKakaoLogin(userInfo);
    }

    private KakaoTokenResponse exchangeKakaoToken(String code) {
        try {
            return kakaoOAuthClient.getToken(code);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new ApplicationException(AuthErrorCase.INVALID_KAKAO_AUTH);
            }
            throw new ApplicationException(AuthErrorCase.KAKAO_API_COMMUNICATION_FAILED);
        } catch (Exception e) {
            throw new ApplicationException(AuthErrorCase.KAKAO_API_COMMUNICATION_FAILED);
        }
    }

    private KakaoUserInfoResponse fetchKakaoUserInfo(String kakaoAccessToken) {
        try {
            return kakaoUserInfoClient.getUserInfo(kakaoAccessToken);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new ApplicationException(AuthErrorCase.INVALID_KAKAO_AUTH);
            }
            throw new ApplicationException(AuthErrorCase.KAKAO_API_COMMUNICATION_FAILED);
        } catch (Exception e) {
            throw new ApplicationException(AuthErrorCase.KAKAO_API_COMMUNICATION_FAILED);
        }
    }
}
