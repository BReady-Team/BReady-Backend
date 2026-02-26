package com.bready.server.recommendation.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.PlaceCandidate;
import com.bready.server.place.domain.PlaceCategoryType;
import com.bready.server.place.dto.PlaceSearchResponse;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.repository.PlaceCandidateRepository;
import com.bready.server.place.service.PlaceSearchService;
import com.bready.server.plan.domain.CategoryState;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.exception.CategoryErrorCase;
import com.bready.server.plan.exception.PlanErrorCase;
import com.bready.server.plan.repository.CategoryStateRepository;
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

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceRecommendationService {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 20;
    private static final int DEFAULT_RADIUS = 2000;

    private final PlanRepository planRepository;
    private final PlanCategoryRepository planCategoryRepository;
    private final TriggerRepository triggerRepository;
    private final PlaceCandidateRepository placeCandidateRepository;
    private final PlaceSearchService placeSearchService;
    private final CategoryStateRepository categoryStateRepository;

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

        if (!trigger.getPlan().getId().equals(request.planId())) {
            throw new ApplicationException(TriggerErrorCase.TRIGGER_NOT_FOUND);
        }
        if (!trigger.getCategory().getId().equals(request.categoryId())) {
            throw new ApplicationException(TriggerErrorCase.TRIGGER_NOT_FOUND);
        }

        int limit = normalizeSize(query.size());
        int radius = (query.radius() != null) ? query.radius() : DEFAULT_RADIUS;
        String region = firstNonBlank(query.region(), plan.getRegion());

        Coordinate base = resolveBaseCoordinate(trigger.getTriggerType(), category.getId(), query);

        String keyword = buildKeyword(region, trigger.getTriggerType());
        PlaceCategoryType categoryType = category.getCategoryType();

        List<PlaceSearchResponse> candidates = placeSearchService.search(
                categoryType,
                keyword,
                base.latitude(),
                base.longitude(),
                radius
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

    private Coordinate resolveBaseCoordinate(TriggerType triggerType, Long categoryId, PlaceRecommendationQuery query) {

        // trigger 중 현재 위치 기반으로 탐색해야 하는 trigger
        if (triggerType == TriggerType.FATIGUE || triggerType == TriggerType.DISTANCE_TOO_FAR) {
            if (query.latitude() == null || query.longitude() == null) {
                throw new ApplicationException(PlaceErrorCase.LOCATION_REQUIRED);
            }
            return new Coordinate(query.latitude(), query.longitude());
        }

        CategoryState state = categoryStateRepository.findByCategory_Id(categoryId)
                .orElseThrow(() -> new ApplicationException(TriggerErrorCase.CATEGORY_STATE_NOT_FOUND));

        Long representativeCandidateId = state.getCurrentCandidateId();
        if (representativeCandidateId == null) {
            throw new ApplicationException(TriggerErrorCase.CATEGORY_STATE_NOT_FOUND);
        }

        // 기존 대표장소 기반 trigger
        PlaceCandidate candidate = placeCandidateRepository
                .findAliveByIdWithCategoryAndPlace(representativeCandidateId)
                .orElseThrow(() -> new ApplicationException(PlaceErrorCase.PLACE_CANDIDATE_NOT_FOUND));

        Double lat = toDouble(candidate.getPlace().getLatitude());
        Double lng = toDouble(candidate.getPlace().getLongitude());

        if (lat == null || lng == null) {
            throw new ApplicationException(PlaceErrorCase.LOCATION_REQUIRED);
        }

        return new Coordinate(lat, lng);

    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
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

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
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

    private record Coordinate(Double latitude, Double longitude) {}
}