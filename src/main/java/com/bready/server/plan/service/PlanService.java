package com.bready.server.plan.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.dto.PlanCreateRequest;
import com.bready.server.plan.dto.PlanCreateResponse;
import com.bready.server.plan.dto.PlanUpdateRequest;
import com.bready.server.plan.dto.PlanUpdateResponse;
import com.bready.server.plan.exception.PlanErrorCase;
import com.bready.server.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional
    public PlanCreateResponse createPlan(Long userId, PlanCreateRequest request) {

        Plan plan = Plan.create(userId, request.getTitle(), request.getPlanDate(), request.getRegion());

        Plan saved = planRepository.save(plan);

        return PlanCreateResponse.builder()
                .planId(saved.getId())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public PlanUpdateResponse updatePlan(Long userId, Long planId, PlanUpdateRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        if (!plan.getOwnerId().equals(userId)) {
            throw new ApplicationException(PlanErrorCase.PLAN_ACCESS_DENIED);
        }

        plan.update(
                request.getTitle(),
                request.getPlanDate(),
                request.getRegion());

        return PlanUpdateResponse.builder()
                .planId(plan.getId())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
