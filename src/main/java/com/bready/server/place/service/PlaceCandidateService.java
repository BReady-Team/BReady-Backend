package com.bready.server.place.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.Place;
import com.bready.server.place.domain.PlaceCandidate;
import com.bready.server.place.dto.PlaceCandidateCreateRequest;
import com.bready.server.place.dto.PlaceCandidateCreateResponse;
import com.bready.server.place.dto.PlaceSummaryResponse;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.repository.PlaceCandidateRepository;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.repository.PlanCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceCandidateService {

    private final PlanCategoryRepository planCategoryRepository;
    private final PlaceCandidateRepository placeCandidateRepository;
    private final PlacePersistenceService placePersistenceService;

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
            saved = placeCandidateRepository.save(PlaceCandidate.create(category, place));
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
}