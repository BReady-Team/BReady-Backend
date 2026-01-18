package com.bready.server.recommendation.port;

import java.util.List;

public interface PlaceRecommendationPort {

    List<Long> recommendPlaceCandidates(Long categoryId, Long userId);
}
