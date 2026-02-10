package com.bready.server.plan.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.dto.PlanCategoryCreateRequest;
import com.bready.server.plan.dto.PlanCategoryCreateResponse;
import com.bready.server.plan.dto.PlanCategoryDeleteResponse;
import com.bready.server.plan.exception.CategoryErrorCase;
import com.bready.server.plan.exception.PlanErrorCase;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanCategoryService {

    private final PlanRepository planRepository;
    private final PlanCategoryRepository planCategoryRepository;

    @Transactional
    public PlanCategoryCreateResponse addCategory(Long userId, Long planId, PlanCategoryCreateRequest request) {

        Plan plan = planRepository.findByIdAndDeletedAtIsNull(planId)
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        if (!plan.getOwnerId().equals(userId)) {
            throw new ApplicationException(CategoryErrorCase.CATEGORY_ACCESS_DENIED);
        }

        Integer sequence = request.getSequence();
        if (sequence == null || sequence < 1) {
            throw new ApplicationException(CategoryErrorCase.INVALID_SEQUENCE);
        }

        PlanCategory saved = planCategoryRepository.save(
                PlanCategory.create(plan, request.getCategoryType(), sequence)
        );

        return PlanCategoryCreateResponse.builder()
                .planCategoryId(saved.getId())
                .planId(plan.getId())
                .categoryType(saved.getCategoryType())
                .sequence(saved.getSequence())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public PlanCategoryDeleteResponse deleteCategory(
            Long userId,
            Long planId,
            Long planCategoryId
    ) {
        Plan plan = planRepository.findByIdAndDeletedAtIsNull(planId)
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        if (!plan.getOwnerId().equals(userId)) {
            throw new ApplicationException(CategoryErrorCase.CATEGORY_ACCESS_DENIED);
        }

        PlanCategory category = planCategoryRepository
                .findByIdAndPlan_IdAndDeletedAtIsNull(planCategoryId, planId)
                .orElseThrow(() -> new ApplicationException(CategoryErrorCase.CATEGORY_NOT_FOUND));

        category.softDelete();

        return PlanCategoryDeleteResponse.builder()
                .planId(planId)
                .planCategoryId(planCategoryId)
                .deletedAt(category.getDeletedAt())
                .build();
    }
}
