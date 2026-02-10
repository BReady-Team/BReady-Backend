package com.bready.server.plan.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.dto.*;
import com.bready.server.plan.exception.CategoryErrorCase;
import com.bready.server.plan.exception.PlanErrorCase;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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


    @Transactional
    public PlanCategoryOrderUpdateResponse updateCategoryOrder(
            Long userId,
            Long planId,
            PlanCategoryOrderUpdateRequest request
    ) {

        Plan plan = planRepository.findByIdAndDeletedAtIsNull(planId)
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        if (!plan.getOwnerId().equals(userId)) {
            throw new ApplicationException(PlanErrorCase.PLAN_ACCESS_DENIED);
        }

        List<PlanCategoryOrderUpdateRequest.OrderItem> orders = request.getOrders();
        if (orders == null || orders.isEmpty()) {
            throw new ApplicationException(CategoryErrorCase.INVALID_ORDERS);
        }

        // 중복, 누락 검증
        Set<Long> ids = orders.stream()
                .map(PlanCategoryOrderUpdateRequest.OrderItem::getPlanCategoryId)
                .collect(Collectors.toSet());

        if (ids.size() != orders.size()) {
            throw new ApplicationException(CategoryErrorCase.INVALID_ORDERS);
        }

        // plan 소속 + soft delete 차단 일괄 조회
        List<PlanCategory> categories = planCategoryRepository.findAllByPlan_IdAndDeletedAtIsNullOrderBySequenceAsc(planId);

        if (categories.size() != orders.size()) {
            throw new ApplicationException(CategoryErrorCase.INVALID_ORDERS);
        }

        Set<Long> categoryIds = categories.stream()
                .map(PlanCategory::getId)
                .collect(Collectors.toSet());

        if (!categoryIds.equals(ids)) {
            throw new ApplicationException(CategoryErrorCase.INVALID_ORDERS);
        }

        Map<Long, Integer> sequenceMap = orders.stream()
                .collect(Collectors.toMap(
                        PlanCategoryOrderUpdateRequest.OrderItem::getPlanCategoryId,
                        PlanCategoryOrderUpdateRequest.OrderItem::getSequence
                ));

        for (PlanCategory category : categories) {
            Integer seq = sequenceMap.get(category.getId());
            if (seq == null || seq < 1) {
                throw new ApplicationException(CategoryErrorCase.INVALID_SEQUENCE);
            }
            category.updateSequence(seq);
        }

        return PlanCategoryOrderUpdateResponse.builder()
                .planId(planId)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
