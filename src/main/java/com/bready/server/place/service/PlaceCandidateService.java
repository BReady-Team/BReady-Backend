package com.bready.server.place.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.Place;
import com.bready.server.place.domain.PlaceCandidate;
import com.bready.server.place.dto.PlaceCandidateCreateRequest;
import com.bready.server.place.dto.PlaceCandidateCreateResponse;
import com.bready.server.place.dto.PlaceCandidateRepresentativeResponse;
import com.bready.server.place.dto.PlaceSummaryResponse;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.repository.PlaceCandidateRepository;
import com.bready.server.plan.domain.CategorySelectionLog;
import com.bready.server.plan.domain.CategoryState;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.repository.CategorySelectionLogRepository;
import com.bready.server.plan.repository.CategoryStateRepository;
import com.bready.server.plan.repository.PlanCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlaceCandidateService {

    private final PlanCategoryRepository planCategoryRepository;
    private final PlaceCandidateRepository placeCandidateRepository;
    private final PlacePersistenceService placePersistenceService;
    private final CategoryStateRepository categoryStateRepository;
    private final CategorySelectionLogRepository categorySelectionLogRepository;

    @Transactional
    public PlaceCandidateCreateResponse createCandidate(PlaceCandidateCreateRequest request) {

        // planId + categoryId 매칭 검증
        PlanCategory category = planCategoryRepository
                .findByIdAndPlan_Id(request.categoryId(), request.planId())
                .orElseThrow(() -> ApplicationException.from(PlaceErrorCase.INVALID_PLAN_OR_CATEGORY));

        // Place는 중복이면 재사용 (REQUIRES_NEW로 분리된 빈 호출)
        Place place = placePersistenceService.getOrCreate(request);

        // PlaceCandidate 저장 -> 동시성 중복은 DB 유니크로 차단
        PlaceCandidate saved;
        try {
            saved = placeCandidateRepository.saveAndFlush(PlaceCandidate.create(category, place));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.from(PlaceErrorCase.DUPLICATE_PLACE_CANDIDATE);
        }

        return PlaceCandidateCreateResponse.builder()
                .candidateId(saved.getId())
                .place(PlaceSummaryResponse.builder()
                        .id(place.getId())
                        .externalId(place.getExternalId())
                        .name(place.getName())
                        .address(place.getAddress())
                        .isIndoor(place.getIsIndoor())
                        .build())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public PlaceCandidateRepresentativeResponse setRepresentative(Long candidateId) {

        PlaceCandidate candidate = placeCandidateRepository
                .findByIdWithCategoryAndPlace(candidateId)
                .orElseThrow(() ->
                        ApplicationException.from(PlaceErrorCase.PLACE_CANDIDATE_NOT_FOUND)
                );

        PlanCategory category = candidate.getCategory();
        Long categoryId = category.getId();

        CategoryState state = categoryStateRepository
                .findByCategory_Id(categoryId)
                .orElse(null);

        if (state == null) {
            // 첫 대표 지정
            state = CategoryState.create(category, candidateId);
            categoryStateRepository.save(state);
        } else {
            // 이미 대표인 경우
            if (state.isRepresentative(candidateId)) {
                throw ApplicationException.from(
                        PlaceErrorCase.ALREADY_REPRESENTATIVE_CANDIDATE
                );
            }
            state.changeRepresentative(candidateId);
        }

        categorySelectionLogRepository.save(
                CategorySelectionLog.of(
                        categoryId,
                        candidateId,
                        LocalDateTime.now()
                )
        );

        return PlaceCandidateRepresentativeResponse.builder()
                .categoryId(categoryId)
                .representativeCandidateId(candidateId)
                .changedAt(state.getUpdatedAt())
                .build();
    }
}