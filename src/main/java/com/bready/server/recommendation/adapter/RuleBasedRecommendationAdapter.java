package com.bready.server.recommendation.adapter;

import com.bready.server.recommendation.port.PlaceRecommendationPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RuleBasedRecommendationAdapter implements PlaceRecommendationPort {
    @Override
    public List<Long> recommendPlaceCandidates(Long categoryId, Long userId) {
        // 로직만 잡아놓은거니깐 해당 파일 or 해당 로직 아예 삭제하고 처음부터 구현해도 됩니다.
        return List.of();
    }
}
