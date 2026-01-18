package com.bready.server.trigger.repository;

import com.bready.server.trigger.domain.Decision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
}
