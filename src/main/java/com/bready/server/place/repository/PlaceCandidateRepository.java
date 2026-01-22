package com.bready.server.place.repository;

import com.bready.server.place.domain.PlaceCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCandidateRepository extends JpaRepository<PlaceCandidate, Long> {
}