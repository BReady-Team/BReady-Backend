package com.bready.server.place.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.Place;
import com.bready.server.place.domain.PlaceCandidate;
import com.bready.server.place.dto.PlaceCandidateCreateRequest;
import com.bready.server.place.dto.PlaceCandidateCreateResponse;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.repository.PlaceCandidateRepository;
import com.bready.server.place.repository.PlaceRepository;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.repository.PlanCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceCandidateService {

    private final PlaceRepository placeRepository;
    private final PlaceCandidateRepository placeCandidateRepository;
    private final PlanCategoryRepository planCategoryRepository;

    @Transactional
    public PlaceCandidateCreateResponse createCandidate(PlaceCandidateCreateRequest request) {

        // planId + categoryId 연관 검증
        PlanCategory category = planCategoryRepository
                .findByIdAndPlan_Id(request.categoryId(), request.planId())
                .orElseThrow(() ->
                        ApplicationException.from(PlaceErrorCase.INVALID_PLAN_OR_CATEGORY)
                );

        Place place = getOrCreatePlace(request);

        // 후보 저장 (중복은 DB 유니크 제약으로 차단)
        PlaceCandidate saved;
        try {
            saved = placeCandidateRepository.save(
                    PlaceCandidate.create(category, place)
            );
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.from(PlaceErrorCase.DUPLICATE_PLACE_CANDIDATE);
        }

        return PlaceCandidateCreateResponse.builder()
                .candidateId(saved.getId())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // Place 조회 또는 생성은 별도 트랜잭션으로 분리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Place getOrCreatePlace(PlaceCandidateCreateRequest request) {
        try {
            return placeRepository.findByExternalId(request.externalId())
                    .orElseGet(() ->
                            placeRepository.save(
                                    Place.create(
                                            request.externalId(),
                                            request.name(),
                                            request.address(),
                                            request.latitude(),
                                            request.longitude(),
                                            request.isIndoor()
                                    )
                            )
                    );
        } catch (DataIntegrityViolationException e) {
            // 다른 트랜잭션이 먼저 insert 한 경우
            return placeRepository.findByExternalId(request.externalId())
                    .orElseThrow(() ->
                            ApplicationException.from(PlaceErrorCase.DUPLICATE_PLACE)
                    );
        }
    }
}
