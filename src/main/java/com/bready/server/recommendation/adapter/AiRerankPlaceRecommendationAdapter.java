package com.bready.server.recommendation.adapter;

import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.recommendation.ai.AiRerankResult;
import com.bready.server.recommendation.ai.AiRerankService;
import com.bready.server.recommendation.dto.PlaceRecommendationResponse;
import com.bready.server.recommendation.port.PlaceRecommendationPort;
import com.bready.server.recommendation.service.PlaceRerankCandidate;
import com.bready.server.trigger.domain.TriggerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;
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
            int limit,
            String excludeExternalId
    ) {

        log.info("[AI] Rerank adapter activated - trigger={}, region={}", triggerType, region);

        List<PlaceRecommendationResponse.RecommendationItem> base =
                ruleBasedAdapter.recommendPlaceCandidates(category, triggerType, region, latitude, longitude, radius, limit, excludeExternalId);

        if (base.isEmpty()) return base;

        List<PlaceRerankCandidate> targets = base.stream()
                .map(PlaceRerankCandidate::from)
                .toList();

        String context = buildContext(triggerType, region);
        AiRerankResult result = aiRerankService.rerank(context, targets);

        if (result == null) return base;

        List<String> rankedIds = Optional.ofNullable(result.rankedIds()).orElse(List.of());
        if (rankedIds.isEmpty()) return base;

        Map<String, PlaceRecommendationResponse.RecommendationItem> itemMap =
                base.stream().collect(Collectors.toMap(
                        PlaceRecommendationResponse.RecommendationItem::externalId,
                        i -> i,
                        (a,b) -> a
                ));

        List<PlaceRecommendationResponse.RecommendationItem> reordered = new ArrayList<>();
        Set<String> added = new HashSet<>();

        for (String id : rankedIds) {
            if (excludeExternalId != null && excludeExternalId.equals(id)) continue;
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
                        item.distanceMeters(),
                        reason
                ));
                added.add(id);
            }
        }

        for (PlaceRecommendationResponse.RecommendationItem item : base) {
            if (!added.contains(item.externalId())) {
                reordered.add(item);
            }
        }

        return reordered.isEmpty() ? base : reordered;
    }

    private String buildContext(TriggerType triggerType, String region) {
        return "trigger=" + triggerType + (region != null ? ", region=" + region : "");
    }
}
