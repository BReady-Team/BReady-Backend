package com.bready.server.recommendation.adapter;

import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.recommendation.ai.AiRerankResult;
import com.bready.server.recommendation.ai.AiRerankService;
import com.bready.server.recommendation.dto.PlaceRecommendationResponse;
import com.bready.server.recommendation.port.PlaceRecommendationPort;
import com.bready.server.recommendation.service.PlaceRerankCandidate;
import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.domain.TriggerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "recommendation.ai", name = "enabled", havingValue = "true")
public class AiRerankPlaceRecommendationAdapter implements PlaceRecommendationPort {

    private final RuleBasedRecommendationAdapter ruleBasedAdapter;
    private final AiRerankService aiRerankService;

    @Override
    public List<PlaceRecommendationResponse.RecommendationItem> recommendPlaceCandidates(
            PlanCategory category,
            TriggerType triggerType,
            String region,
            Double latitude,
            Double longitude,
            int radius,
            int limit
    ) {

        log.debug("[AI] Rerank adapter activated - trigger={}, region={}", triggerType, region);

        List<PlaceRecommendationResponse.RecommendationItem> base =
                ruleBasedAdapter.recommendPlaceCandidates(category, triggerType, region, latitude, longitude, radius, limit);

        if (base.isEmpty()) return base;

        List<PlaceRerankCandidate> targets = base.stream()
                .map(PlaceRerankCandidate::from)
                .toList();

        String context = buildContext(triggerType, region);
        AiRerankResult result = aiRerankService.rerank(context, targets);

        if (result.rankedIds() == null || result.rankedIds().isEmpty()) {
            return base;
        }

        Map<String, PlaceRecommendationResponse.RecommendationItem> itemMap =
                base.stream().collect(Collectors.toMap(
                        PlaceRecommendationResponse.RecommendationItem::externalId,
                        i -> i
                ));

        List<PlaceRecommendationResponse.RecommendationItem> reordered = new ArrayList<>();

        for (String id : result.rankedIds()) {
            PlaceRecommendationResponse.RecommendationItem item = itemMap.get(id);
            if (item != null) {
                String reason = result.reasonsById() != null
                        ? result.reasonsById().getOrDefault(id, item.reason())
                        : item.reason();

                reordered.add(new PlaceRecommendationResponse.RecommendationItem(
                        item.externalId(),
                        item.name(),
                        item.address(),
                        item.latitude(),
                        item.longitude(),
                        item.isIndoor(),
                        reason
                ));
            }
        }

        return reordered.isEmpty() ? base : reordered;
    }

    private String buildContext(TriggerType triggerType, String region) {
        return "trigger=" + triggerType + (region != null ? ", region=" + region : "");
    }
}
