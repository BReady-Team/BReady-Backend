package com.bready.server.trigger.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.repository.PlaceCandidateRepository;
import com.bready.server.plan.domain.CategoryState;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.repository.CategoryStateRepository;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.stats.event.TriggerCreatedEvent;
import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.dto.TriggerCreateRequest;
import com.bready.server.trigger.dto.TriggerCreateResponse;
import com.bready.server.trigger.exception.TriggerErrorCase;
import com.bready.server.trigger.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TriggerService {

    private final PlanCategoryRepository planCategoryRepository;
    private final TriggerRepository triggerRepository;
    private final CategoryStateRepository categoryStateRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PlaceCandidateRepository placeCandidateRepository;

    @Transactional
    public TriggerCreateResponse createTrigger(TriggerCreateRequest request) {

        PlanCategory category = planCategoryRepository
                .findByIdAndPlan_IdAndDeletedAtIsNull(request.categoryId(), request.planId())
                .orElseThrow(() ->
                        ApplicationException.from(TriggerErrorCase.PLAN_OR_CATEGORY_NOT_FOUND)
                );

        CategoryState state = categoryStateRepository
                .findByCategory_Id(category.getId())
                .orElseThrow(() ->
                        ApplicationException.from(TriggerErrorCase.CATEGORY_STATE_NOT_FOUND)
                );

        Long currentCandidateId = state.getCurrentCandidateId();

        // 후보 장소가 없을 때 트리거 발생 차단
        if (currentCandidateId == null) {
            throw ApplicationException.from(TriggerErrorCase.CATEGORY_STATE_NOT_FOUND);
        }

        // 대표 후보 alive 검증
        boolean alive = placeCandidateRepository.existsAliveByIdAndCategoryId(currentCandidateId, category.getId());
        if (!alive) {
            throw ApplicationException.from(TriggerErrorCase.CATEGORY_STATE_NOT_FOUND);
        }

        Trigger trigger = triggerRepository.save(
                Trigger.create(
                        category.getPlan(),
                        category,
                        currentCandidateId,
                        request.triggerType()
                )
        );

        Long planId = category.getPlan().getId();
        LocalDateTime occurredAt = trigger.getOccurredAt();

        // 이벤트 발행
        eventPublisher.publishEvent(new TriggerCreatedEvent(planId,occurredAt));

        return TriggerCreateResponse.builder()
                .triggerId(trigger.getId())
                .occurredAt(trigger.getOccurredAt())
                .build();
    }
}