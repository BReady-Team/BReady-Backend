package com.bready.server.auth.service;

import com.bready.server.auth.domain.RefreshToken;
import com.bready.server.auth.dto.*;
import com.bready.server.auth.exception.AuthErrorCase;
import com.bready.server.auth.repository.RefreshTokenRepository;
import com.bready.server.global.config.security.jwt.JwtTokenProvider;
import com.bready.server.user.domain.User;
import com.bready.server.user.domain.UserProfile;
import com.bready.server.user.repository.UserProfileRepository;
import com.bready.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.user.exception.UserErrorCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        // 1) 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApplicationException(UserErrorCase.DUPLICATED_EMAIL);
        }

        try {

            // 2) User 저장
            String encoded = passwordEncoder.encode(request.getPassword());
            User user = userRepository.save(User.createLocal(request.getEmail(), encoded));

            // 3) UserProfile 저장
            UserProfile profile = userProfileRepository.save(
                    UserProfile.create(user, request.getNickname())
            );

            // 4) 응답 구성
            String createdAt = user.getCreatedAt() == null
                    ? null
                    : user.getCreatedAt()
                    .atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            return SignupResponse.builder()
                    .userId(user.getId())
                    .nickname(profile.getNickname())
                    .email(user.getEmail())
                    .createdAt(createdAt)
                    .build();
        } catch (DataIntegrityViolationException e) {
            // existsByEmail 통과 후 동시성으로 unique 제약 위반 발생 가능
            throw new ApplicationException(UserErrorCase.DUPLICATED_EMAIL, e);
        }
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApplicationException(AuthErrorCase.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApplicationException(AuthErrorCase.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .key(String.valueOf(user.getId()))
                        .token(refreshToken)
                        .userId(user.getId())
                        .build()
        );

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {

        // 1) refresh 토큰 검증
        jwtTokenProvider.validateRefreshToken(request.getRefreshToken());

        // 2) refresh 토큰에서 userId 추출
        Long userId = jwtTokenProvider.getUserId(request.getRefreshToken());

        // 3) Redis 에 저장된 refresh 토큰 조회
        RefreshToken saved = refreshTokenRepository.findById(String.valueOf(userId))
                .orElseThrow(() -> new ApplicationException(AuthErrorCase.REFRESH_TOKEN_INVALID));

        // 4) 토큰 일치 여부 확인
        if (!saved.getToken().equals(request.getRefreshToken())) {
            throw new ApplicationException(AuthErrorCase.REFRESH_TOKEN_INVALID);
        }

        // 5) 새 토큰 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // 6) Redis refresh 토큰 갱신
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .key(String.valueOf(userId))
                        .token(newRefreshToken)
                        .userId(userId)
                        .build()
        );

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
