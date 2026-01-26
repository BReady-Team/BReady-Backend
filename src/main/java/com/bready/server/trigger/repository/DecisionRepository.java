package com.bready.server.trigger.repository;

import com.bready.server.trigger.domain.Decision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
    boolean existsByTrigger_Id(Long triggerId);
    Optional<Decision> findByTrigger_Id(Long triggerId);
}
