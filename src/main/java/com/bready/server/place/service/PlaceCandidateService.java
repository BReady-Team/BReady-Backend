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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceCandidateService {

    private final PlaceRepository placeRepository;
    private final PlaceCandidateRepository placeCandidateRepository;
    private final PlanCategoryRepository planCategoryRepository;

    @Transactional
    public PlaceCandidateCreateResponse createCandidate(PlaceCandidateCreateRequest request) {

        PlanCategory category = planCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ApplicationException.from(PlaceErrorCase.INVALID_PLAN_OR_CATEGORY));

        Place place;
        try {
            place = placeRepository.findByExternalId(request.externalId())
                    .orElseGet(() -> placeRepository.save(
                            Place.create(
                                    request.externalId(),
                                    request.name(),
                                    request.address(),
                                    request.latitude(),
                                    request.longitude(),
                                    request.isIndoor()
                            )
                    ));
        } catch (DataIntegrityViolationException e) {
            // 동시 insert 충돌 → 재조회
            place = placeRepository.findByExternalId(request.externalId())
                    .orElseThrow(() ->
                            ApplicationException.from(PlaceErrorCase.DUPLICATE_PLACE_CANDIDATE)
                    );
        }

        // PlaceCandidate 저장 (중복은 DB가 차단하도록)
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
}

