package com.bready.server.recommendation.port;

import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.recommendation.dto.PlaceRecommendationResponse;
import com.bready.server.trigger.domain.TriggerType;

import java.util.List;

public interface PlaceRecommendationPort {

    List<PlaceRecommendationResponse.RecommendationItem> recommendPlaceCandidates(
            PlanCategory category,
            TriggerType triggerType,
            String region,
            Double latitude,
            Double longitude,
            int radius,
            int limit,
            String excludeExternalId
    );
}
