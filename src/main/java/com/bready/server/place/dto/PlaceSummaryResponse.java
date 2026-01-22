package com.bready.server.place.dto;

import lombok.Builder;

public record PlaceSummaryResponse(
        Long id,
        String externalId,
        String name,
        String address,
        Boolean isIndoor
) {
    @Builder
    public PlaceSummaryResponse {}
}
