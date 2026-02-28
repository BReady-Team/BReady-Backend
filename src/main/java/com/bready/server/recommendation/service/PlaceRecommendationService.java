package com.bready.server.recommendation.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.PlaceCandidate;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.repository.PlaceCandidateRepository;
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
import com.bready.server.recommendation.port.PlaceRecommendationPort;
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
    private final CategoryStateRepository categoryStateRepository;
    private final PlaceRecommendationPort placeRecommendationPort;

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

        ResolvedBase resolved = resolveBase(trigger.getTriggerType(), category.getId(), query);

        String region = firstNonBlank(
                normalizeRegion(query.region()),
                normalizeRegion(resolved.region())
        );

        Coordinate base = resolved.coordinate();

        List<PlaceRecommendationResponse.RecommendationItem> items =
                placeRecommendationPort.recommendPlaceCandidates(
                        category,
                        trigger.getTriggerType(),
                        region,
                        base.latitude(),
                        base.longitude(),
                        radius,
                        limit,
                        resolved.excludeExternalId()
                );

        if (items.isEmpty()) {
            return new PlaceRecommendationResponse(List.of());
        }

        return new PlaceRecommendationResponse(items);
    }

    private ResolvedBase resolveBase(TriggerType triggerType, Long categoryId, PlaceRecommendationQuery query) {

        // trigger 중 현재 위치 기반으로 탐색해야 하는 trigger
        if (triggerType == TriggerType.FATIGUE || triggerType == TriggerType.DISTANCE_TOO_FAR) {
            if (query.latitude() == null || query.longitude() == null) {
                throw new ApplicationException(PlaceErrorCase.LOCATION_REQUIRED);
            }
            return new ResolvedBase(
                    new Coordinate(query.latitude(), query.longitude()), null, null
            );
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

        String regionFromAddress = extractRegionFromAddress(candidate.getPlace().getAddress());
        String excludeExternalId = candidate.getPlace().getExternalId();

        return new ResolvedBase(new Coordinate(lat, lng), regionFromAddress, excludeExternalId);
    }

    private String extractRegionFromAddress(String address) {
        if (address == null) return null;
        String a = address.trim();
        if (a.isEmpty()) return null;

        String[] parts = a.split("\\s+");
        if (parts.length >= 2) return parts[0] + " " + parts[1];
        if (parts.length == 1) return parts[0];
        return null;
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private int normalizeSize(Integer size) {
        if (size == null) return DEFAULT_SIZE;
        return Math.max(1, Math.min(size, MAX_SIZE));
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private String normalizeRegion(String region) {
        if (region == null) return null;
        String r = region.trim();
        if (r.isEmpty()) return null;
        if ("string".equalsIgnoreCase(r)) return null;
        if ("null".equalsIgnoreCase(r)) return null;
        return r;
    }

    private record Coordinate(Double latitude, Double longitude) {}

    private record ResolvedBase(Coordinate coordinate, String region, String excludeExternalId) {}
}