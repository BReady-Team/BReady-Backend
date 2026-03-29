package com.bready.server.recommendation.adapter;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.PlaceCategoryType;
import com.bready.server.place.dto.PlaceSearchResponse;
import com.bready.server.place.exception.PlaceErrorCase;
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
            int limit,
            String excludeExternalId
    ) {

        PlaceCategoryType categoryType = category.getCategoryType();
        String keyword = normalizedRegion(region);

        List<PlaceSearchResponse> candidates;

        try {
            candidates = placeSearchService.search(
                    categoryType,
                    keyword,
                    latitude,
                    longitude,
                    radius
            );
        } catch (ApplicationException e) {
            if (e.getErrorCase().getErrorCode().equals(PlaceErrorCase.PLACE_NOT_FOUND.getErrorCode())) {
                return List.of();
            }
            throw e;
        }

        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        return candidates.stream()
                .filter(p -> excludeExternalId == null || !excludeExternalId.equals(p.externalId()))
                .limit(limit)
                .map(p -> {
                    Integer distanceMeters = null;

                    if (latitude != null && longitude != null && p.latitude() != null && p.longitude() != null) {
                        distanceMeters = (int) Math.round(calculateDistanceMeters(latitude, longitude, p.latitude().doubleValue(), p.longitude().doubleValue()));
                    }

                    return new PlaceRecommendationResponse.RecommendationItem(
                            p.externalId(),
                            p.name(),
                            p.address(),
                            p.latitude(),
                            p.longitude(),
                            p.isIndoor(),
                            distanceMeters,
                            buildReason(triggerType)
                    );
                }).toList();
    }

    private String normalizedRegion(String region) {
        if (region == null) return null;
        String r = region.trim();
        if (r.isEmpty()) return null;
        if ("string".equalsIgnoreCase(r)) return null;
        if ("null".equalsIgnoreCase(r)) return null;
        return r;
    }

    private String buildReason(TriggerType triggerType) {
        return switch (triggerType) {
            case WEATHER_BAD -> "날씨 악화 상황을 고려해 실내 이동이 가능한 장소를 추천합니다.";
            case WAITING_TOO_LONG -> "혼잡을 피하기 위해 주변 대체 후보를 추천합니다.";
            case PLACE_CLOSED -> "영업 종료 상황을 고려해 주변 대체 장소를 추천합니다.";
            case FATIGUE -> "휴식이 가능한 성격의 장소를 우선 추천합니다.";
            case DISTANCE_TOO_FAR -> "이동 부담을 줄이기 위해 가까운 후보를 우선 추천합니다.";
        };
    }

    // 거리 계산 메서드
    private double calculateDistanceMeters(Double lat1, Double lng1, Double lat2, Double lng2) {
        final int R = 6371000; // 지구 반지름 (m)

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
