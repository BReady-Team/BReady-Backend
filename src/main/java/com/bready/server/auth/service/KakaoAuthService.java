package com.bready.server.auth.service;

import com.bready.server.auth.client.KakaoOAuthClient;
import com.bready.server.auth.client.KakaoUserInfoClient;
import com.bready.server.auth.dto.*;
import com.bready.server.auth.exception.AuthErrorCase;
import com.bready.server.global.exception.ApplicationException;
import com.bready.server.user.domain.User;
import com.bready.server.user.domain.UserAuthProvider;
import com.bready.server.user.domain.UserProfile;
import com.bready.server.user.exception.UserErrorCase;
import com.bready.server.user.repository.UserProfileRepository;
import com.bready.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoUserInfoClient kakaoUserInfoClient;

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TokenIssuer tokenIssuer;

    public KakaoLoginResponse login(KakaoLoginRequest request) {

        // 인가 코드 검증
        String code = request.getCode();
        if (code == null || code.isBlank()) {
            throw new ApplicationException(AuthErrorCase.INVALID_KAKAO_AUTH);
        }

        // 카카오 토큰 교환
        KakaoTokenResponse token = exchangeKakaoToken(code);
        String kakaoAccessToken = token.getAccessToken();
        if (kakaoAccessToken == null || kakaoAccessToken.isBlank()) {
            throw new ApplicationException(AuthErrorCase.INVALID_KAKAO_AUTH);
        }

        // 사용자 정보 조회
        KakaoUserInfoResponse userInfo = fetchKakaoUserInfo(kakaoAccessToken);
        if (userInfo.getId() == null) {
            throw new ApplicationException(AuthErrorCase.INVALID_KAKAO_AUTH);
        }

        return processKakaoLogin(userInfo);
    }

    @Transactional
    protected KakaoLoginResponse processKakaoLogin(KakaoUserInfoResponse userInfo) {

        String providerUserId = String.valueOf(userInfo.getId());

        User user = userRepository
                .findByAuthProviderAndProviderUserId(UserAuthProvider.KAKAO, providerUserId)
                .orElse(null);

        boolean isNewUser = false;

        if (user == null) {

            String email = (userInfo.getKakaoAccount() == null) ? null : userInfo.getKakaoAccount().getEmail();
            if (email == null || email.isBlank()) {
                throw new ApplicationException(UserErrorCase.KAKAO_EMAIL_CONSENT_REQUIRED);
            }

            if (userRepository.existsByEmail(email)) {
                throw new ApplicationException(UserErrorCase.DUPLICATED_EMAIL);
            }

            String nickname = (userInfo.getProperties() == null) ? null : userInfo.getProperties().getNickname();
            if (nickname == null || nickname.isBlank()) {
                nickname = generateRandomNickname();
            }

            isNewUser = true;
            try {
                user = userRepository.save(
                        User.createSocial(UserAuthProvider.KAKAO, providerUserId, email)
                );
                userProfileRepository.save(UserProfile.create(user, nickname));
            } catch (DataIntegrityViolationException e) {
                user = userRepository
                        .findByAuthProviderAndProviderUserId(UserAuthProvider.KAKAO, providerUserId)
                        .orElseThrow(() -> e);
                isNewUser = false;
            }
        }

        // 토큰 발급
        TokenResponse tokens = tokenIssuer.issue(user.getId());

        User userWithProfile = userRepository.findByIdWithProfile(user.getId())
                .orElseThrow(() -> new ApplicationException(AuthErrorCase.INVALID_KAKAO_AUTH));

        String joinedAt = userWithProfile.getCreatedAt() == null ? null
                : userWithProfile.getCreatedAt().atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String nicknameForResponse = userWithProfile.getUserProfile() != null
                ? userWithProfile.getUserProfile().getNickname()
                : null;

        return KakaoLoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .isNewUser(isNewUser)
                .user(KakaoLoginResponse.UserDto.builder()
                        .userId(user.getId())
                        .nickname(nicknameForResponse)
                        .email(user.getEmail())
                        .joinedAt(joinedAt)
                        .build())
                .build();
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

    private String generateRandomNickname() {
        return "사용자" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
