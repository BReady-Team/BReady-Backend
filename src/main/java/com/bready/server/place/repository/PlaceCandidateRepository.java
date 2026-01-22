package com.bready.server.place.repository;

import com.bready.server.place.domain.PlaceCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PlaceCandidateRepository extends JpaRepository<PlaceCandidate, Long> {
    @Query("""
        select pc
        from PlaceCandidate pc
        join fetch pc.category c
        join fetch pc.place p
        where pc.id = :candidateId
    """)
    Optional<PlaceCandidate> findByIdWithCategoryAndPlace(Long candidateId);
}