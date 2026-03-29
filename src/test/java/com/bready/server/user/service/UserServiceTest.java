package com.bready.server.user.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.s3.service.S3Uploader;
import com.bready.server.user.domain.User;
import com.bready.server.user.domain.UserProfile;
import com.bready.server.user.dto.UserProfileDto;
import com.bready.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Uploader s3Uploader;

    @Mock
    private User user;

    @Mock
    private UserProfile userProfile;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("getMyProfile")
    class GetMyProfileTest {

        @Test
        @DisplayName("내 정보 조회 성공")
        void getMyProfile_success() {
            LocalDateTime createdAt = LocalDateTime.of(2026, Month.MARCH, 29, 10, 0, 0);

            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.of(user));
            given(user.getUserProfile()).willReturn(userProfile);
            given(user.getId()).willReturn(1L);
            given(user.getEmail()).willReturn("test@test.com");
            given(user.getCreatedAt()).willReturn(createdAt);

            given(userProfile.getNickname()).willReturn("testNickname");
            given(userProfile.getBio()).willReturn("testBio");
            given(userProfile.getProfileImageUrl()).willReturn("imageUrl");

            UserProfileDto result = userService.getMyProfile(1L);

            assertThat(result.userId()).isEqualTo(1L);
            assertThat(result.nickname()).isEqualTo("testNickname");
            assertThat(result.email()).isEqualTo("test@test.com");
            assertThat(result.bio()).isEqualTo("testBio");
            assertThat(result.profileImageUrl()).isEqualTo("imageUrl");
            assertThat(result.joinedAt()).startsWith("2026-03-29T10:00:00");
        }

        @Test
        @DisplayName("userId가 null이면 AUTH_REQUIRED 예외")
        void getMyProfile_authRequired() {
            assertThatThrownBy(() -> userService.getMyProfile(null))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        @DisplayName("유저가 없으면 USER_NOT_FOUND 예외")
        void getMyProfile_userNotFound() {
            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getMyProfile(1L))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        @DisplayName("프로필이 없으면 USER_NOT_FOUND 예외")
        void getMyProfile_profileNotFound() {
            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.of(user));
            given(user.getUserProfile()).willReturn(null);

            assertThatThrownBy(() -> userService.getMyProfile(1L))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        @DisplayName("createdAt이 null이면 joinedAt도 null")
        void getMyProfile_joinedAtNull() {
            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.of(user));
            given(user.getUserProfile()).willReturn(userProfile);
            given(user.getId()).willReturn(1L);
            given(user.getEmail()).willReturn("test@test.com");
            given(user.getCreatedAt()).willReturn(null);

            given(userProfile.getNickname()).willReturn("testNickname");
            given(userProfile.getBio()).willReturn("testBio");
            given(userProfile.getProfileImageUrl()).willReturn("imageUrl");

            UserProfileDto result = userService.getMyProfile(1L);

            assertThat(result.joinedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("updateNickname")
    class UpdateNicknameTest {

        @Test
        @DisplayName("닉네임 수정 성공")
        void updateNickname_success() {
            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.of(user));
            given(user.getUserProfile()).willReturn(userProfile);

            userService.updateNickname(1L, "newNickname");

            then(userProfile).should().changeNickname("newNickname");
        }

        @Test
        @DisplayName("유저가 없으면 USER_NOT_FOUND 예외")
        void updateNickname_userNotFound() {
            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateNickname(1L, "newNickname"))
                    .isInstanceOf(ApplicationException.class);
        }
    }

    @Nested
    @DisplayName("updateBio")
    class UpdateBioTest {

        @Test
        @DisplayName("자기소개 수정 성공")
        void updateBio_success() {
            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.of(user));
            given(user.getUserProfile()).willReturn(userProfile);

            userService.updateBio(1L, "new bio");

            then(userProfile).should().changeBio("new bio");
        }

        @Test
        @DisplayName("유저가 없으면 USER_NOT_FOUND 예외")
        void updateBio_userNotFound() {
            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateBio(1L, "new bio"))
                    .isInstanceOf(ApplicationException.class);
        }
    }

    @Nested
    @DisplayName("updateProfileImage")
    class UpdateProfileImageTest {

        @Test
        @DisplayName("프로필 이미지 수정 성공 - 기존 이미지도 삭제")
        void updateProfileImage_success() {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.png",
                    "image/png",
                    "content".getBytes()
            );

            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.of(user));
            given(user.getUserProfile()).willReturn(userProfile);
            given(userProfile.getProfileImageUrl()).willReturn("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/old.png");

            given(s3Uploader.uploadAndReturnKey(file, "profiles")).willReturn("profiles/new.png");
            given(s3Uploader.buildUrl("profiles/new.png"))
                    .willReturn("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/new.png");

            String result = userService.updateProfileImage(1L, file);

            assertThat(result).isEqualTo("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/new.png");

            InOrder inOrder = inOrder(s3Uploader, userProfile);
            inOrder.verify(s3Uploader).uploadAndReturnKey(file, "profiles");
            inOrder.verify(s3Uploader).buildUrl("profiles/new.png");
            inOrder.verify(userProfile).changeProfileImage("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/new.png");
            inOrder.verify(s3Uploader).delete("profiles/old.png");
        }

        @Test
        @DisplayName("기존 이미지가 없으면 delete 호출 안 함")
        void updateProfileImage_withoutOldImage() {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.png",
                    "image/png",
                    "content".getBytes()
            );

            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.of(user));
            given(user.getUserProfile()).willReturn(userProfile);
            given(userProfile.getProfileImageUrl()).willReturn(null);

            given(s3Uploader.uploadAndReturnKey(file, "profiles")).willReturn("profiles/new.png");
            given(s3Uploader.buildUrl("profiles/new.png"))
                    .willReturn("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/new.png");

            String result = userService.updateProfileImage(1L, file);

            assertThat(result).isEqualTo("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/new.png");
            then(userProfile).should().changeProfileImage("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/new.png");
            then(s3Uploader).should(never()).delete(any());
        }

        @Test
        @DisplayName("기존 이미지 URL이 공백이면 delete 호출 안 함")
        void updateProfileImage_blankOldImage() {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.png",
                    "image/png",
                    "content".getBytes()
            );

            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.of(user));
            given(user.getUserProfile()).willReturn(userProfile);
            given(userProfile.getProfileImageUrl()).willReturn("   ");

            given(s3Uploader.uploadAndReturnKey(file, "profiles")).willReturn("profiles/new.png");
            given(s3Uploader.buildUrl("profiles/new.png"))
                    .willReturn("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/new.png");

            userService.updateProfileImage(1L, file);

            then(s3Uploader).should(never()).delete(any());
        }

        @Test
        @DisplayName("기존 이미지 URL이 S3 형식이 아니면 delete 호출 안 함")
        void updateProfileImage_invalidOldImageUrl() {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.png",
                    "image/png",
                    "content".getBytes()
            );

            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.of(user));
            given(user.getUserProfile()).willReturn(userProfile);
            given(userProfile.getProfileImageUrl()).willReturn("https://example.com/old.png");

            given(s3Uploader.uploadAndReturnKey(file, "profiles")).willReturn("profiles/new.png");
            given(s3Uploader.buildUrl("profiles/new.png"))
                    .willReturn("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/new.png");

            userService.updateProfileImage(1L, file);

            then(s3Uploader).should(never()).delete(any());
        }

        @Test
        @DisplayName("기존 이미지 삭제 실패여도 새 이미지 변경은 성공")
        void updateProfileImage_deleteFailButSuccess() {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.png",
                    "image/png",
                    "content".getBytes()
            );

            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.of(user));
            given(user.getUserProfile()).willReturn(userProfile);
            given(userProfile.getProfileImageUrl()).willReturn("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/old.png");

            given(s3Uploader.uploadAndReturnKey(file, "profiles")).willReturn("profiles/new.png");
            given(s3Uploader.buildUrl("profiles/new.png"))
                    .willReturn("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/new.png");
            willThrow(new RuntimeException("s3 delete fail"))
                    .given(s3Uploader).delete("profiles/old.png");

            String result = userService.updateProfileImage(1L, file);

            assertThat(result).isEqualTo("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/new.png");
            then(userProfile).should().changeProfileImage("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profiles/new.png");
        }

        @Test
        @DisplayName("유저가 없으면 USER_NOT_FOUND 예외")
        void updateProfileImage_userNotFound() {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.png",
                    "image/png",
                    "content".getBytes()
            );

            given(userRepository.findByIdWithProfile(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateProfileImage(1L, file))
                    .isInstanceOf(ApplicationException.class);
        }
    }
}
