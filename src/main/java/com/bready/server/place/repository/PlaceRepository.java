package com.bready.server.place.repository;

import com.bready.server.place.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findByExternalId(String externalId);
}
