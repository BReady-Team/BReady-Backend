package com.bready.server.place.repository;

import com.bready.server.place.domain.Place;
import com.bready.server.place.domain.PlaceCandidate;
import com.bready.server.plan.domain.PlanCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCandidateRepository extends JpaRepository<PlaceCandidate, Long> {
    boolean existsByCategoryAndPlace(PlanCategory category, Place place);
}