package com.bready.server.global.config.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key signingKey;
    private final long accessExp;
    private final long refreshExp;

    public JwtTokenProvider(JwtProperties properties) {

        String secret = properties.getSecret();
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT Secret은 최소 32바이트 이상이어야 합니다.");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExp = properties.getAccessExp();
        this.refreshExp = properties.getRefreshExp();

        log.info("JWT 서명 키 초기화 완료");
    }

    public String generateAccessToken(Long userId) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessExp))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshExp))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.parseLong(
                Jwts.parserBuilder()
                        .setSigningKey(signingKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject()
        );
    }

    // TODO: Refresh Token Redis 저장 및 토큰 재발급 API 구현 후에 사용
    public Long getTokenExpiry(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .getTime();
    }
}
