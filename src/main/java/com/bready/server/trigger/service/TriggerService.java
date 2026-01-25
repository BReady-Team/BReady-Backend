package com.bready.server.trigger.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.plan.repository.PlanRepository;
import com.bready.server.stats.service.PlanStatsService;
import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.dto.TriggerCreateRequest;
import com.bready.server.trigger.dto.TriggerCreateResponse;
import com.bready.server.trigger.exception.TriggerErrorCase;
import com.bready.server.trigger.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TriggerService {

    private final PlanRepository planRepository;
    private final PlanCategoryRepository planCategoryRepository;
    private final TriggerRepository triggerRepository;
    private final PlanStatsService planStatsService;

    @Transactional
    public TriggerCreateResponse createTrigger(TriggerCreateRequest request) {

        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() ->
                        ApplicationException.from(TriggerErrorCase.PLAN_OR_CATEGORY_NOT_FOUND)
                );

        // TODO: 인증 도입 후 플랜 소유자 검증 로직 추가 필요
        // if (!category.getPlan().isOwnedBy(currentUserId)) {
        //     throw ApplicationException.from(TriggerErrorCase.NO_TRIGGER_PERMISSION);
        // }

        PlanCategory category = planCategoryRepository
                .findByIdAndPlan_Id(request.categoryId(), request.planId())
                .orElseThrow(() ->
                        ApplicationException.from(TriggerErrorCase.PLAN_OR_CATEGORY_NOT_FOUND)
                );

        Trigger trigger = triggerRepository.save(
                Trigger.create(plan, category, request.triggerType())
        );

        // 통계 증가 (결정/장소 변경 없음)
        planStatsService.increaseTriggerCount(category.getPlan().getId());

        return TriggerCreateResponse.builder()
                .triggerId(trigger.getId())
                .occurredAt(trigger.getOccurredAt())
                .build();
    }
}