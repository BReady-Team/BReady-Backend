package com.bready.server.place.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PlaceCandidateRepresentativeResponse(
        Long categoryId,
        Long representativeCandidateId,
        LocalDateTime changedAt
) {}
