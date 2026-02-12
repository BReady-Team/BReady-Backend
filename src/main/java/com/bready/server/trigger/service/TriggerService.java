package com.bready.server.trigger.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.CategoryState;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.repository.CategoryStateRepository;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.stats.event.TriggerCreatedEvent;
import com.bready.server.stats.service.PlanStatsService;
import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.dto.TriggerCreateRequest;
import com.bready.server.trigger.dto.TriggerCreateResponse;
import com.bready.server.trigger.exception.TriggerErrorCase;
import com.bready.server.trigger.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TriggerService {

    private final PlanCategoryRepository planCategoryRepository;
    private final TriggerRepository triggerRepository;
    private final PlanStatsService planStatsService;
    private final CategoryStateRepository categoryStateRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TriggerCreateResponse createTrigger(TriggerCreateRequest request) {

        PlanCategory category = planCategoryRepository
                .findByIdAndPlan_Id(request.categoryId(), request.planId())
                .orElseThrow(() ->
                        ApplicationException.from(TriggerErrorCase.PLAN_OR_CATEGORY_NOT_FOUND)
                );

        CategoryState state = categoryStateRepository
                .findByCategory_Id(category.getId())
                .orElseThrow(() ->
                        ApplicationException.from(TriggerErrorCase.CATEGORY_STATE_NOT_FOUND)
                );

        Long currentCandidateId = state.getCurrentCandidateId();

        Trigger trigger = triggerRepository.save(
                Trigger.create(
                        category.getPlan(),
                        category,
                        currentCandidateId,
                        request.triggerType()
                )
        );

        Long planId = category.getPlan().getId();

        // 이벤트 발행
        eventPublisher.publishEvent(new TriggerCreatedEvent(planId));

        return TriggerCreateResponse.builder()
                .triggerId(trigger.getId())
                .occurredAt(trigger.getOccurredAt())
                .build();
    }
}