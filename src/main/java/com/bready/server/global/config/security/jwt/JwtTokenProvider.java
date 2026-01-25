package com.bready.server.global.config.security.jwt;

import com.bready.server.auth.exception.AuthErrorCase;
import com.bready.server.global.exception.ApplicationException;
import io.jsonwebtoken.*;
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
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT Secret은 비어있을 수 없습니다.");
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT Secret은 최소 32바이트 이상이어야 합니다.");
        }

        long accessExp = properties.getAccessExp();
        long refreshExp = properties.getRefreshExp();
        if (accessExp <= 0 || refreshExp <= 0) {
            throw new IllegalStateException("JWT 만료 시간은 0보다 커야 합니다.");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExp = accessExp;
        this.refreshExp = refreshExp;

        log.info("JWT 서명 키 및 만료 시간 초기화 완료");
    }

    public String generateAccessToken(Long userId) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("typ", "access")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessExp))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("typ", "refresh")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshExp))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return "access".equals(claims.get("typ"));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (!"refresh".equals(claims.get("typ"))) {
                throw new ApplicationException(AuthErrorCase.INVALID_TOKEN);
            }
        } catch (ExpiredJwtException e) {
            throw new ApplicationException(AuthErrorCase.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApplicationException(AuthErrorCase.INVALID_TOKEN);
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
