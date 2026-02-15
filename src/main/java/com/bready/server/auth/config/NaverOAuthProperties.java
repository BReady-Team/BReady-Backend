package com.bready.server.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth.naver")
public class NaverOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
