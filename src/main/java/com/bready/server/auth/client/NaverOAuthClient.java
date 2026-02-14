package com.bready.server.auth.client;

import com.bready.server.auth.config.NaverOAuthProperties;
import com.bready.server.auth.dto.NaverTokenResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class NaverOAuthClient {

    private static final String TOKEN_URI = "https://nid.naver.com/oauth2.0/token";
    private final NaverOAuthProperties properties;
    private final WebClient webClient;

    public NaverOAuthClient(
            NaverOAuthProperties properties,
            @Qualifier("naverWebClient") WebClient webClient
    ) {
        this.properties = properties;
        this.webClient = webClient;
    }

    public NaverTokenResponse getToken(String authorizationCode, String state) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", properties.getClientId());
        formData.add("client_secret", properties.getClientSecret());
        formData.add("redirect_uri", properties.getRedirectUri());
        formData.add("code", authorizationCode);
        formData.add("state", state);

        return webClient.post()
                .uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(NaverTokenResponse.class)
                .block(java.time.Duration.ofSeconds(5));
    }
}
