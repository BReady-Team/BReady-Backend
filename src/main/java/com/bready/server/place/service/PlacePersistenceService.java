package com.bready.server.place.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.Place;
import com.bready.server.place.dto.PlaceCandidateCreateRequest;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlacePersistenceService {

    private final PlaceRepository placeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Place getOrCreate(PlaceCandidateCreateRequest request) {
        try {
            return placeRepository.findByExternalId(request.externalId())
                    .orElseGet(() -> placeRepository.saveAndFlush(
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
            // 다른 트랜잭션이 먼저 insert 한 경우는 재조회로 해결
            return placeRepository.findByExternalId(request.externalId())
                    .orElseThrow(() -> ApplicationException.from(PlaceErrorCase.PLACE_SAVE_FAILED));
        }
    }
}
