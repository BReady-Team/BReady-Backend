package com.bready.server.place.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.Place;
import com.bready.server.place.domain.PlaceCandidate;
import com.bready.server.place.dto.PlaceCandidateCreateRequest;
import com.bready.server.place.dto.PlaceCandidateCreateResponse;
import com.bready.server.place.dto.PlaceSummaryResponse;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.repository.PlaceCandidateRepository;
import com.bready.server.place.repository.PlaceRepository;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.repository.PlanCategoryRepository;
import lombok.RequiredArgsConstructor;
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
        PlanCategory category = planCategoryRepository
                .findByIdAndPlan_Id(request.categoryId(), request.planId())
                .orElseThrow(() -> ApplicationException.from(PlaceErrorCase.INVALID_PLAN_OR_CATEGORY));

        Place place = placeRepository.findByExternalId(request.externalId())
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

        // category + place 중복 방지
        if (placeCandidateRepository.existsByCategoryAndPlace(category, place)) {
            throw ApplicationException.from(PlaceErrorCase.DUPLICATE_PLACE_CANDIDATE);
        }

        // 장소 후보 저장
        PlaceCandidate saved = placeCandidateRepository.save(
                PlaceCandidate.create(category, place)
        );

        return PlaceCandidateCreateResponse.builder()
                .candidateId(saved.getId())
                .place(
                        PlaceSummaryResponse.builder()
                                .id(place.getId())
                                .externalId(place.getExternalId())
                                .name(place.getName())
                                .address(place.getAddress())
                                .isIndoor(place.getIsIndoor())
                                .build()
                )
                .createdAt(saved.getCreatedAt())
                .build();
    }
}

