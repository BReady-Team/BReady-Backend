package com.bready.server.recommendation.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.PlaceCategoryType;
import com.bready.server.place.dto.PlaceSearchResponse;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.service.PlaceSearchService;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.exception.CategoryErrorCase;
import com.bready.server.plan.exception.PlanErrorCase;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.plan.repository.PlanRepository;
import com.bready.server.recommendation.dto.PlaceRecommendationQuery;
import com.bready.server.recommendation.dto.PlaceRecommendationRequest;
import com.bready.server.recommendation.dto.PlaceRecommendationResponse;
import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.domain.TriggerType;
import com.bready.server.trigger.exception.TriggerErrorCase;
import com.bready.server.trigger.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceRecommendationService {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 20;

    private final PlanRepository planRepository;
    private final PlanCategoryRepository planCategoryRepository;
    private final TriggerRepository triggerRepository;
    private final PlaceSearchService placeSearchService;

    @Transactional(readOnly = true)
    public PlaceRecommendationResponse recommendPlaces(Long userId, PlaceRecommendationRequest request, PlaceRecommendationQuery query) {

        // plan 존재 + 소유 검증
        Plan plan = planRepository.findByIdAndDeletedAtIsNull(request.planId())
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        if (!plan.getOwnerId().equals(userId)) {
            throw new ApplicationException(PlanErrorCase.PLAN_ACCESS_DENIED);
        }

        // category 존재 + plan 매칭 검증
        PlanCategory category = planCategoryRepository.findByIdAndPlan_IdAndDeletedAtIsNull(request.categoryId(), request.planId())
                .orElseThrow(() -> new ApplicationException(CategoryErrorCase.CATEGORY_NOT_FOUND));

        // trigger 존재 검증
        Trigger trigger = triggerRepository.findById(request.triggerId())
                .orElseThrow(() -> new ApplicationException(TriggerErrorCase.TRIGGER_NOT_FOUND));

        int limit = normalizeSize(query.size());

        String keyword = buildKeyword(query.region(), trigger.getTriggerType());

        PlaceCategoryType categoryType = category.getCategoryType();

        List<PlaceSearchResponse> candidates = placeSearchService.search(
                categoryType,
                keyword,
                query.latitude(),
                query.longitude(),
                query.radius()
        );

        if (candidates == null || candidates.isEmpty()) {
            throw new ApplicationException(PlaceErrorCase.PLACE_NOT_FOUND);
        }

        List<PlaceRecommendationResponse.RecommendationItem> items = candidates.stream()
                .limit(limit)
                .map(p -> new PlaceRecommendationResponse.RecommendationItem(
                        p.externalId(),
                        p.name(),
                        p.address(),
                        p.latitude(),
                        p.longitude(),
                        p.isIndoor(),
                        buildReason(trigger.getTriggerType())
                ))
                .toList();

        if (items.isEmpty()) {
            throw new ApplicationException(PlaceErrorCase.PLACE_NOT_FOUND);
        }

        return new PlaceRecommendationResponse(items);
    }

    private int normalizeSize(Integer size) {
        if (size == null) return DEFAULT_SIZE;
        return Math.max(1, Math.min(size, MAX_SIZE));
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