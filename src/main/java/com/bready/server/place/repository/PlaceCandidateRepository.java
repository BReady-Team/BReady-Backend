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

    @Query("""
        select pc
        from PlaceCandidate pc
        join fetch pc.category c
        where pc.id = :candidateId
    """)
    Optional<PlaceCandidate> findByIdWithCategory(Long candidateId);

    @Query("""
        select pc
        from PlaceCandidate pc
        where pc.id = :candidateId
          and pc.deletedAt is null
    """)
    Optional<PlaceCandidate> findAliveById(Long candidateId);

    // 카테고리 일치 + 살아있는 후보 검증
    @Query("""
        select (count(pc) > 0)
        from PlaceCandidate pc
        where pc.id = :candidateId
          and pc.category.id = :categoryId
          and pc.deletedAt is null
    """)
    boolean existsAliveByIdAndCategoryId(Long candidateId, Long categoryId);
}