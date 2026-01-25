package com.bready.server.user.controller;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.user.domain.User;
import com.bready.server.user.domain.UserProfile;
import com.bready.server.user.dto.UserProfileDto;
import com.bready.server.user.exception.UserErrorCase;
import com.bready.server.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "JWT 인증된 사용자의 기본 프로필 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<UserProfileDto> me() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new ApplicationException(UserErrorCase.AUTH_REQUIRED);
        }

        Long userId = (Long) auth.getPrincipal();
        User user = userService.getMe(userId);

        // userProfile 은 join 으로 함께 로딩
        UserProfile profile = user.getUserProfile();

        String joinedAt = user.getCreatedAt() == null
                ? null
                : user.getCreatedAt()
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        UserProfileDto response = UserProfileDto.builder()
                .userId(user.getId())
                .nickname(profile.getNickname())
                .email(user.getEmail())
                .bio(profile.getBio())
                .profileImageUrl(profile.getProfileImageUrl())
                .joinedAt(joinedAt)
                .build();

        return CommonResponse.success(response);
    }
}
