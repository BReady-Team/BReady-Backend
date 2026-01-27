package com.bready.server.trigger.repository;

import com.bready.server.trigger.domain.SwitchLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SwitchLogRepository extends JpaRepository<SwitchLog, Long> {
    boolean existsByDecision_Id(Long decisionId);
}
