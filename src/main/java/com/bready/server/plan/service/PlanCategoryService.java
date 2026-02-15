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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
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

        for (int attempt = 0; attempt < 3; attempt++) {
            PlanCategory last = planCategoryRepository
                    .findLastByPlanIdForUpdate(planId, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .orElse(null);

            int nextSequence = (last == null) ? 1 : last.getSequence() + 1;

            try {
                PlanCategory saved = planCategoryRepository.save(
                        PlanCategory.create(plan, request.getCategoryType(), nextSequence)
                );

                return PlanCategoryCreateResponse.builder()
                        .planCategoryId(saved.getId())
                        .planId(plan.getId())
                        .categoryType(saved.getCategoryType())
                        .sequence(saved.getSequence())
                        .createdAt(saved.getCreatedAt())
                        .build();
            } catch (DataIntegrityViolationException e) {
                // 마지막 시도에 실패 처리
                if (attempt == 2) {
                    throw e;
                }
            }
        }

        throw new IllegalStateException("unreachable");

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

        int n = orders.size();

        Set<Integer> seqSet = orders.stream()
                .map(PlanCategoryOrderUpdateRequest.OrderItem::getSequence)
                .collect(Collectors.toSet());

        if (seqSet.size() != n) {
            throw new ApplicationException(CategoryErrorCase.INVALID_SEQUENCE);
        }

        for (int i = 1; i <= n; i++) {
            if (!seqSet.contains(i)) {
                throw new ApplicationException(CategoryErrorCase.INVALID_SEQUENCE);
            }
        }

        Map<Long, Integer> sequenceMap = orders.stream()
                .collect(Collectors.toMap(
                        PlanCategoryOrderUpdateRequest.OrderItem::getPlanCategoryId,
                        PlanCategoryOrderUpdateRequest.OrderItem::getSequence
                ));

        for (PlanCategory category : categories) {
            Integer seq = sequenceMap.get(category.getId());

            if (seq == null) {
                throw new ApplicationException(CategoryErrorCase.INVALID_ORDERS);
            }

            if (seq < 1) {
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
