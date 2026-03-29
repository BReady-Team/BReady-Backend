package com.bready.server.user.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.exception.GlobalExceptionHandler;
import com.bready.server.user.dto.UpdateBioRequest;
import com.bready.server.user.dto.UpdateNicknameRequest;
import com.bready.server.user.dto.UserProfileDto;
import com.bready.server.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        HandlerMethodArgumentResolver currentUserResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(CurrentUser.class)
                        && parameter.getParameterType().equals(Long.class);
            }

            @Override
            public Object resolveArgument(
                    MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
            ) {
                return userId;
            }
        };

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(currentUserResolver)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyProfile_success() throws Exception {
        UserProfileDto dto = new UserProfileDto(
                userId,
                "testNickname",
                "test@test.com",
                "testBio",
                "imageUrl",
                "2026-03-29"
        );

        given(userService.getMyProfile(userId)).willReturn(dto);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.nickname").value("testNickname"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.bio").value("testBio"))
                .andExpect(jsonPath("$.data.profileImageUrl").value("imageUrl"))
                .andExpect(jsonPath("$.data.joinedAt").value("2026-03-29"));

        verify(userService).getMyProfile(userId);
    }

    @Test
    @DisplayName("닉네임 수정 성공")
    void updateNickname_success() throws Exception {
        UpdateNicknameRequest request = new UpdateNicknameRequest("newNickname");

        mockMvc.perform(patch("/api/v1/users/profile/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).updateNickname(eq(userId), eq("newNickname"));
    }

    @Test
    @DisplayName("자기소개 수정 성공")
    void updateBio_success() throws Exception {
        UpdateBioRequest request = new UpdateBioRequest("new bio");

        mockMvc.perform(patch("/api/v1/users/profile/bio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).updateBio(eq(userId), eq("new bio"));
    }

    @Test
    @DisplayName("프로필 이미지 변경 성공")
    void updateProfileImage_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                MediaType.IMAGE_PNG_VALUE,
                "image-content".getBytes()
        );

        given(userService.updateProfileImage(eq(userId), any()))
                .willReturn("imageUrl");

        mockMvc.perform(multipart("/api/v1/users/profile/image")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("imageUrl"));

        verify(userService).updateProfileImage(eq(userId), any());
    }
}