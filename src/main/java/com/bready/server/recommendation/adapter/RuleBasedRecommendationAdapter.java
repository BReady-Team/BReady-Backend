package com.bready.server.recommendation.adapter;

import com.bready.server.place.domain.PlaceCategoryType;
import com.bready.server.place.dto.PlaceSearchResponse;
import com.bready.server.place.service.PlaceSearchService;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.recommendation.dto.PlaceRecommendationResponse;
import com.bready.server.recommendation.port.PlaceRecommendationPort;
import com.bready.server.trigger.domain.TriggerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RuleBasedRecommendationAdapter implements PlaceRecommendationPort {

    private final PlaceSearchService placeSearchService;

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

        String keyword = buildKeyword(region, triggerType);
        PlaceCategoryType categoryType = category.getCategoryType();

        List<PlaceSearchResponse> candidates = placeSearchService.search(
                categoryType,
                keyword,
                latitude,
                longitude,
                radius
        );

        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        return candidates.stream()
                .limit(limit)
                .map(p -> new PlaceRecommendationResponse.RecommendationItem(
                        p.externalId(),
                        p.name(),
                        p.address(),
                        p.latitude(),
                        p.longitude(),
                        p.isIndoor(),
                        buildReason(triggerType)
                ))
                .toList();
    }

    private String buildKeyword(String region, TriggerType triggerType) {
        String base = triggerKeyword(triggerType);
        if (region == null || region.isBlank()) return base;
        return region + " " + base;
    }

    private String triggerKeyword(TriggerType triggerType) {
        return switch (triggerType) {
            case WEATHER_BAD -> "실내";
            case WAITING_TOO_LONG -> "근처";
            case PLACE_CLOSED -> "대체";
            case FATIGUE -> "휴식";
            case DISTANCE_TOO_FAR -> "근처";
        };
    }

    private String buildReason(TriggerType triggerType) {
        return switch (triggerType) {
            case WEATHER_BAD -> "날씨 악화 상황을 고려해 실내 이동이 가능한 장소를 추천합니다.";
            case WAITING_TOO_LONG -> "혼잡을 피하기 위해 주변 대체 후보를 추천합니다.";
            case PLACE_CLOSED -> "영업 종료 상황을 고려해 주변 대체 장소를 추천합니다.";
            case FATIGUE -> "휴식이 가능한 성격의 장소를 우선 추천합니다.";
            case DISTANCE_TOO_FAR -> "이동 부담을 줄이기 위해 더 가까운 후보를 우선 추천합니다.";
        };
    }
}
