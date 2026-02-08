package com.bready.server.plan.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.dto.*;
import com.bready.server.plan.exception.PlanErrorCase;
import com.bready.server.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public PlanDetailResponse getPlanDetail(Long userId, Long planId) {

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        // TODO : 플랜 공유 기능 도입 시 소유자 외에도 조회 권한 허용
        if (!plan.getOwnerId().equals(userId)) {
            throw new ApplicationException(PlanErrorCase.PLAN_ACCESS_DENIED);
        }

        PlanDto planDto = PlanDto.builder()
                .planId(plan.getId())
                .title(plan.getTitle())
                .planDate(plan.getPlanDate())
                .region(plan.getRegion())
                .status(plan.getStatus())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();

        return PlanDetailResponse.builder()
                .plan(planDto)
                .build();
    }

    @Transactional(readOnly = true)
    public PlanListResponse getMyPlans(Long userId, int page, int size, String order) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "planDate").and(Sort.by(Sort.Direction.DESC, "createdAt")));

        Page<Plan> result = planRepository.findAllByOwnerIdAndDeletedAtIsNull(userId, pageable);

        List<PlanListItemDto> items = result.getContent().stream()
                .map(plan -> PlanListItemDto.builder()
                        .planId(plan.getId())
                        .title(plan.getTitle())
                        .planDate(plan.getPlanDate())
                        .region(plan.getRegion())
                        .status(plan.getStatus())
                        .createdAt(plan.getCreatedAt())
                        .updatedAt(plan.getUpdatedAt())
                        .build())
                .toList();

        PageInfo pageInfo = PageInfo.builder()
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();

        return PlanListResponse.builder()
                .items(items)
                .pageInfo(pageInfo)
                .build();
    }
}
