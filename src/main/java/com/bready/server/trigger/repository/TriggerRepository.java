package com.bready.server.trigger.repository;

import com.bready.server.trigger.domain.Trigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface TriggerRepository extends JpaRepository<Trigger, Long> {
    @Query("""
    select count(t)
    from Trigger t
    where t.plan.ownerId = :userId
      and (:startAt is null or t.occurredAt >= :startAt)
    """)
    long countByUserIdAndPeriod(Long userId, LocalDateTime startAt);
}
