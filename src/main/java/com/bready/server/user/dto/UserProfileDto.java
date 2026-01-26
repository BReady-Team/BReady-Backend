package com.bready.server.user.dto;

import lombok.Builder;

@Builder
public record UserProfileDto(
        Long userId,
        String nickname,
        String email,
        String bio,
        String profileImageUrl,
        String joinedAt
) {}