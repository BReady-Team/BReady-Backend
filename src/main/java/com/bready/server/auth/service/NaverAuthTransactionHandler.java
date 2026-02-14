package com.bready.server.auth.service;

import com.bready.server.auth.dto.NaverLoginResponse;
import com.bready.server.auth.dto.NaverUserInfoResponse;
import com.bready.server.auth.dto.TokenResponse;
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
public class NaverAuthTransactionHandler {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TokenIssuer tokenIssuer;

    @Transactional
    public NaverLoginResponse processNaverLogin(NaverUserInfoResponse userInfo) {

        String providerUserId = userInfo.getResponse().getId();

        User user = userRepository
                .findByAuthProviderAndProviderUserId(UserAuthProvider.NAVER, providerUserId)
                .orElse(null);

        boolean isNewUser = false;

        if (user == null) {
            String email = userInfo.getResponse().getEmail();
            if (email == null || email.isBlank()) {
                throw new ApplicationException(UserErrorCase.NAVER_EMAIL_CONSENT_REQUIRED);
            }

            if (userRepository.existsByEmail(email)) {
                throw new ApplicationException(UserErrorCase.DUPLICATED_EMAIL);
            }

            String nickname = userInfo.getResponse().getNickname();
            if (nickname == null || nickname.isBlank()) {
                nickname = userInfo.getResponse().getName();
            }
            if (nickname == null || nickname.isBlank()) {
                nickname = generateRandomNickname();
            }

            isNewUser = true;

            try {
                user = userRepository.save(
                        User.createSocial(UserAuthProvider.NAVER, providerUserId, email)
                );
                userProfileRepository.save(UserProfile.create(user, nickname));
            } catch (DataIntegrityViolationException e) {
                if (userRepository.existsByEmail(email)) {
                    throw new ApplicationException(UserErrorCase.DUPLICATED_EMAIL);
                }

                isNewUser = false;

                user = userRepository
                        .findByAuthProviderAndProviderUserId(UserAuthProvider.NAVER, providerUserId)
                        .orElseThrow(() -> e);
            }
        }

        TokenResponse tokens = tokenIssuer.issue(user.getId());

        User userWithProfile = userRepository.findByIdWithProfile(user.getId())
                .orElseThrow(() -> new ApplicationException(AuthErrorCase.INVALID_NAVER_AUTH));

        String joinedAt = userWithProfile.getCreatedAt() == null ? null
                : userWithProfile.getCreatedAt().atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String nicknameForResponse = userWithProfile.getUserProfile() != null
                ? userWithProfile.getUserProfile().getNickname()
                : null;

        return NaverLoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .isNewUser(isNewUser)
                .user(NaverLoginResponse.UserDto.builder()
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