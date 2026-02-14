package com.bready.server.auth.service;

import com.bready.server.auth.client.NaverOAuthClient;
import com.bready.server.auth.client.NaverUserInfoClient;
import com.bready.server.auth.dto.NaverLoginResponse;
import com.bready.server.auth.dto.NaverTokenResponse;
import com.bready.server.auth.dto.NaverUserInfoResponse;
import com.bready.server.auth.exception.AuthErrorCase;
import com.bready.server.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class NaverAuthService {

    private final NaverOAuthClient naverOAuthClient;
    private final NaverUserInfoClient naverUserInfoClient;
    private final NaverAuthTransactionHandler naverAuthTransactionHandler;

    public NaverLoginResponse login(String code, String state) {

        if (code == null || code.isBlank()) {
            throw new ApplicationException(AuthErrorCase.INVALID_NAVER_AUTH);
        }
        if (state == null || state.isBlank()) {
            throw new ApplicationException(AuthErrorCase.INVALID_NAVER_AUTH);
        }

        NaverTokenResponse token = exchangeNaverToken(code, state);
        String naverAccessToken = token.getAccessToken();

        if (naverAccessToken == null || naverAccessToken.isBlank()) {
            throw new ApplicationException(AuthErrorCase.INVALID_NAVER_AUTH);
        }

        NaverUserInfoResponse userInfo = fetchNaverUserInfo(naverAccessToken);

        if (userInfo == null || userInfo.getResponse() == null || userInfo.getResponse().getId() == null) {
            throw new ApplicationException(AuthErrorCase.INVALID_NAVER_AUTH);
        }

        return naverAuthTransactionHandler.processNaverLogin(userInfo);
    }

    private NaverTokenResponse exchangeNaverToken(String code, String state) {
        try {
            NaverTokenResponse token = naverOAuthClient.getToken(code, state);

            if (token != null && token.getError() != null) {
                throw new ApplicationException(AuthErrorCase.INVALID_NAVER_AUTH);
            }

            return token;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new ApplicationException(AuthErrorCase.INVALID_NAVER_AUTH);
            }
            throw new ApplicationException(AuthErrorCase.NAVER_API_COMMUNICATION_FAILED);
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationException(AuthErrorCase.NAVER_API_COMMUNICATION_FAILED);
        }
    }

    private NaverUserInfoResponse fetchNaverUserInfo(String naverAccessToken) {
        try {
            return naverUserInfoClient.getUserInfo(naverAccessToken);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new ApplicationException(AuthErrorCase.INVALID_NAVER_AUTH);
            }
            throw new ApplicationException(AuthErrorCase.NAVER_API_COMMUNICATION_FAILED);
        } catch (Exception e) {
            throw new ApplicationException(AuthErrorCase.NAVER_API_COMMUNICATION_FAILED);
        }
    }
}