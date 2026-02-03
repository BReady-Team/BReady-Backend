package com.bready.server.trigger.repository;

import com.bready.server.trigger.domain.SwitchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface SwitchLogRepository extends JpaRepository<SwitchLog, Long> {
    boolean existsByDecision_Id(Long decisionId);

    @Query("""
    select count(sl)
    from SwitchLog sl
    join sl.decision d
    join d.trigger t
    where t.plan.ownerId = :userId
      and (:startAt is null or sl.createdAt >= :startAt)
    """)
    long countByUserIdAndPeriod(Long userId, LocalDateTime startAt);
}
