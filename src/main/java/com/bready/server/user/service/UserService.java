package com.bready.server.user.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.user.domain.User;
import com.bready.server.user.domain.UserProfile;
import com.bready.server.user.dto.UserProfileDto;
import com.bready.server.user.exception.UserErrorCase;
import com.bready.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserProfileDto getMyProfile(Long userId) {
        if (userId == null) {
            throw new ApplicationException(UserErrorCase.AUTH_REQUIRED);
        }

        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            throw new ApplicationException(UserErrorCase.USER_NOT_FOUND);
        }

        String joinedAt = user.getCreatedAt() == null
                ? null
                : user.getCreatedAt()
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return UserProfileDto.builder()
                .userId(user.getId())
                .nickname(profile.getNickname())
                .email(user.getEmail())
                .bio(profile.getBio())
                .profileImageUrl(profile.getProfileImageUrl())
                .joinedAt(joinedAt)
                .build();
    }
}

