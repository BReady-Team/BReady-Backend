package com.bready.server.auth.service;

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

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoAuthTransactionHandler {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TokenIssuer tokenIssuer;

    @Transactional
    public KakaoLoginResponse processKakaoLogin(KakaoUserInfoResponse userInfo) {

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
                if (userRepository.existsByEmail(email)) {
                    throw new ApplicationException(UserErrorCase.DUPLICATED_EMAIL);
                }

                isNewUser = false;

                user = userRepository
                        .findByAuthProviderAndProviderUserId(UserAuthProvider.KAKAO, providerUserId)
                        .orElseThrow(() -> e);

            }
        }

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

    private String generateRandomNickname() {
        return "사용자" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
