package com.bready.server.recommendation.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.exception.CategoryErrorCase;
import com.bready.server.plan.exception.PlanErrorCase;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.plan.repository.PlanRepository;
import com.bready.server.recommendation.dto.CategoryRecommendationRequest;
import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.exception.TriggerErrorCase;
import com.bready.server.trigger.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendValidationService {

    private final PlanRepository planRepository;
    private final PlanCategoryRepository planCategoryRepository;
    private final TriggerRepository triggerRepository;

    @Transactional(readOnly = true)
    public void validateUserAndLoad(Long userId, CategoryRecommendationRequest request) {

        Plan plan = planRepository.findByIdAndDeletedAtIsNull(request.planId())
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        if (!plan.getOwnerId().equals(userId)) {
            throw new ApplicationException(PlanErrorCase.PLAN_ACCESS_DENIED);
        }

        PlanCategory currentCategory = planCategoryRepository.findByIdAndPlan_IdAndDeletedAtIsNull(request.categoryId(), request.planId())
                .orElseThrow(() -> new ApplicationException(CategoryErrorCase.CATEGORY_NOT_FOUND));

        Trigger trigger = triggerRepository.findById(request.triggerId())
                .orElseThrow(() -> new ApplicationException(TriggerErrorCase.TRIGGER_NOT_FOUND));

        if (!trigger.getPlan().getId().equals(request.planId()) || !trigger.getCategory().getId().equals(request.categoryId())) {
            throw new ApplicationException(TriggerErrorCase.TRIGGER_NOT_FOUND);
        }

        planCategoryRepository.findAllByPlan_IdAndDeletedAtIsNullOrderBySequenceAsc(request.planId());
    }
}