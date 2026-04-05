package com.bready.server.plan.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.CategoryState;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.dto.*;
import com.bready.server.plan.exception.PlanErrorCase;
import com.bready.server.plan.repository.CategoryStateRepository;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.plan.repository.PlanRepository;
import com.bready.server.user.domain.User;
import com.bready.server.user.domain.UserProfile;
import com.bready.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanCategoryRepository planCategoryRepository;
    private final CategoryStateRepository categoryStateRepository;
    private final UserRepository userRepository;

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
        Plan plan = planRepository.findByIdAndDeletedAtIsNull(planId)
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

        Plan plan = planRepository.findByIdAndDeletedAtIsNull(planId)
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        // TODO : 플랜 공유 기능 도입 시 소유자 외에도 조회 권한 허용
        if (!plan.getOwnerId().equals(userId)) {
            throw new ApplicationException(PlanErrorCase.PLAN_ACCESS_DENIED);
        }

        return buildPlanDetailResponse(plan);
    }

    @Transactional(readOnly = true)
    public PlanDetailResponse getSharedPlanDetail(String shareToken) {
        Plan plan = planRepository.findByShareTokenAndDeletedAtIsNull(shareToken)
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        return buildPlanDetailResponse(plan);
    }

    private PlanDetailResponse buildPlanDetailResponse(Plan plan) {
        PlanDto planDto = buildPlanDto(plan);

        List<PlanCategory> categories = planCategoryRepository.findAllDetailByPlanId(plan.getId());

        if (categories.isEmpty()) {
            return PlanDetailResponse.builder()
                    .plan(planDto)
                    .categories(List.of())
                    .build();
        }

        List<Long> categoryIds = categories.stream()
                .map(PlanCategory::getId)
                .toList();

        Map<Long, Long> representativeMap = categoryStateRepository.findAllByCategory_IdIn(categoryIds)
                .stream()
                .filter(cs -> cs.getCurrentCandidateId() != null)
                .collect(Collectors.toMap(
                        cs -> cs.getCategory().getId(),
                        CategoryState::getCurrentCandidateId
                ));

        List<PlanDetailCategoryDto> categoryDtos = categories.stream()
                .map(category -> buildCategoryDto(category, representativeMap.get(category.getId())))
                .toList();

        return PlanDetailResponse.builder()
                .plan(planDto)
                .categories(categoryDtos)
                .build();
    }


    private PlanDto buildPlanDto(Plan plan) {
        User user = userRepository.findByIdWithProfile(plan.getOwnerId())
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        UserProfile profile = user.getUserProfile();

        String ownerNickname = profile != null ? profile.getNickname() : "사용자";
        String ownerProfileImageUrl = profile != null ? profile.getProfileImageUrl() : null;

        return PlanDto.builder()
                .planId(plan.getId())
                .title(plan.getTitle())
                .planDate(plan.getPlanDate())
                .region(plan.getRegion())
                .status(plan.getStatus())
                .ownerNickname(ownerNickname)
                .ownerProfileImageUrl(ownerProfileImageUrl)
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private PlanDetailCategoryDto buildCategoryDto(PlanCategory category, Long representativeId) {
        List<PlanDetailCandidateDto> candidateDtos = category.getCandidates().stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .map(candidate -> {
                    boolean isRep = representativeId != null && representativeId.equals(candidate.getId());

                    return PlanDetailCandidateDto.builder()
                            .candidateId(candidate.getId())
                            .isRepresentative(isRep)
                            .place(PlanDetailPlaceDto.builder()
                                    .id(candidate.getPlace().getId())
                                    .externalId(candidate.getPlace().getExternalId())
                                    .name(candidate.getPlace().getName())
                                    .address(candidate.getPlace().getAddress())
                                    .latitude(candidate.getPlace().getLatitude())
                                    .longitude(candidate.getPlace().getLongitude())
                                    .isIndoor(candidate.getPlace().getIsIndoor())
                                    .build())
                            .build();
                })
                .toList();

        return PlanDetailCategoryDto.builder()
                .planCategoryId(category.getId())
                .categoryType(category.getCategoryType())
                .sequence(category.getSequence())
                .representativeCandidateId(representativeId)
                .candidates(candidateDtos)
                .build();
    }


    @Transactional(readOnly = true)
    public PlanListResponse getMyPlans(Long userId, int page, int size, SortDirection order) {
        Sort.Direction direction = (order == SortDirection.ASC) ? Sort.Direction.ASC : Sort.Direction.DESC;

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

    @Transactional
    public PlanDeleteResponse deletePlan(Long userId, Long planId) {

        Plan plan = planRepository.findByIdAndDeletedAtIsNull(planId)
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        if (!plan.getOwnerId().equals(userId)) {
            throw new ApplicationException(PlanErrorCase.PLAN_ACCESS_DENIED);
        }

        plan.softDelete();

        return PlanDeleteResponse.builder()
                .planId(plan.getId())
                .deletedAt(plan.getDeletedAt())
                .build();
    }

    @Transactional
    public String getOrCreateShareToken(Long userId, Long planId) {
        Plan plan = planRepository.findByIdAndDeletedAtIsNullForUpdate(planId)
                .orElseThrow(() -> new ApplicationException(PlanErrorCase.PLAN_NOT_FOUND));

        if (!plan.getOwnerId().equals(userId)) {
            throw new ApplicationException(PlanErrorCase.PLAN_ACCESS_DENIED);
        }

        if (plan.getShareToken() != null) {
            return plan.getShareToken();
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        plan.setShareToken(token);

        return token;
    }
}
