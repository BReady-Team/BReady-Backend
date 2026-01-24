package com.bready.server.place.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record PlaceCandidateDeleteResponse(
        Long candidateId,
        LocalDateTime deletedAt
) {}
