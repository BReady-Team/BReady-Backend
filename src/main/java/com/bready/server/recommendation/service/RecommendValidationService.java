package com.bready.server.recommendation.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.exception.PlanErrorCase;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.recommendation.dto.CategoryRecommendationRequest;
import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.exception.TriggerErrorCase;
import com.bready.server.trigger.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendValidationService {

    private final PlanCategoryRepository planCategoryRepository;
    private final TriggerRepository triggerRepository;

    @Transactional(readOnly = true)
    public Validated validateUserAndLoad(Long userId, CategoryRecommendationRequest request) {

        Trigger trigger = triggerRepository.findByIdAndDeletedAtIsNull(request.triggerId())
                .orElseThrow(() -> new ApplicationException(TriggerErrorCase.TRIGGER_NOT_FOUND));

        Plan plan = trigger.getPlan();
        if (!plan.getOwnerId().equals(userId)) {
            throw new ApplicationException(PlanErrorCase.PLAN_ACCESS_DENIED);
        }

        PlanCategory currentCategory = trigger.getCategory();

        List<PlanCategory> planCategories = planCategoryRepository.findAllByPlan_IdAndDeletedAtIsNullOrderBySequenceAsc(plan.getId());
        return new Validated(plan, currentCategory, trigger, planCategories);
    }

    public record Validated(Plan plan, PlanCategory currentCategory, Trigger trigger, List<PlanCategory> planCategories) {}
}