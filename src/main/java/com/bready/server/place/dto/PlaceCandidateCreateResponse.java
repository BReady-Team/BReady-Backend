package com.bready.server.place.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PlaceCandidateCreateResponse(
        Long candidateId,
        LocalDateTime createdAt
) {}