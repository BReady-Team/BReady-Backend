package com.bready.server.auth.client;

import com.bready.server.auth.dto.NaverUserInfoResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class NaverUserInfoClient {

    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";

    private final WebClient webClient;

    public NaverUserInfoClient(@Qualifier("naverWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public NaverUserInfoResponse getUserInfo(String naverAccessToken) {
        return webClient.get()
                .uri(USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + naverAccessToken)
                .retrieve()
                .bodyToMono(NaverUserInfoResponse.class)
                .block(java.time.Duration.ofSeconds(5));
    }
}