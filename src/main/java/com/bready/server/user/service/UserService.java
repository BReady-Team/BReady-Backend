package com.bready.server.user.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.s3.service.S3Uploader;
import com.bready.server.user.domain.User;
import com.bready.server.user.domain.UserProfile;
import com.bready.server.user.dto.UserProfileDto;
import com.bready.server.user.exception.UserErrorCase;
import com.bready.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

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

    @Transactional
    public void updateNickname(Long userId, String nickname) {

        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        UserProfile profile = user.getUserProfile();

        profile.changeNickname(nickname);
    }

    @Transactional
    public void updateBio(Long userId, String bio) {

        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        UserProfile profile = user.getUserProfile();

        profile.changeBio(bio);
    }

    @Transactional
    public String updateProfileImage(Long userId, MultipartFile file) {

        if (userId == null) {
            throw new ApplicationException(UserErrorCase.AUTH_REQUIRED);
        }

        // 사용자 조회
        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        UserProfile profile = user.getUserProfile();

        // 기존 이미지 삭제 (있을 경우)
        if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isBlank()) {

            String oldKey = extractKeyFromUrl(profile.getProfileImageUrl());

            if (oldKey != null) {
                s3Uploader.delete(oldKey);
            }
        }

        // 새 이미지 업로드
        String fileKey = s3Uploader.uploadAndReturnKey(file, "profiles");

        // 공개 URL 생성
        String url = s3Uploader.buildUrl(fileKey);

        profile.changeProfileImage(url);

        return url;
    }

    private String extractKeyFromUrl(String url) {
        int index = url.indexOf(".amazonaws.com/");

        if (index == -1) { return null;}

        return url.substring(index + ".amazonaws.com/".length());
    }
}

