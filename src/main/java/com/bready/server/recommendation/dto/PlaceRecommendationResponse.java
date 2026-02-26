package com.bready.server.recommendation.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

public record PlaceRecommendationResponse(
        List<RecommendationItem> items
) {

    public record RecommendationItem(
            String externalId,
            String name,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            Boolean isIndoor,
            String reason
    ) {}

}
