package com.bready.server.auth.service;

import com.bready.server.auth.domain.RefreshToken;
import com.bready.server.auth.dto.TokenResponse;
import com.bready.server.auth.repository.RefreshTokenRepository;
import com.bready.server.global.config.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenIssuer {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse issue(Long userId) {

        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .key(String.valueOf(userId))
                        .token(refreshToken)
                        .userId(userId)
                        .build()
        );

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
