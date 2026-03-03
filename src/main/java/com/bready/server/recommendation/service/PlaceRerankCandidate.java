package com.bready.server.recommendation.service;

import com.bready.server.recommendation.ai.AiRerankTarget;
import com.bready.server.recommendation.dto.PlaceRecommendationResponse;

import java.util.HashMap;
import java.util.Map;

public record PlaceRerankCandidate(
        String id,
        String name,
        String address,
        Boolean isIndoor
) implements AiRerankTarget {

    public static PlaceRerankCandidate from(PlaceRecommendationResponse.RecommendationItem item) {
        return new PlaceRerankCandidate(
                item.externalId(),
                item.name(),
                item.address(),
                item.isIndoor()
        );
    }

    @Override
    public Map<String, Object> toPromptAttributes() {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("address", address);
        m.put("isIndoor", isIndoor);
        return m;
    }
}
