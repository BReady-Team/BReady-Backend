package com.bready.server.trigger.repository;

import com.bready.server.trigger.domain.Decision;
import com.bready.server.trigger.domain.DecisionType;
import com.bready.server.trigger.domain.TriggerType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
    interface KeepDecisionActivityRow {
        Long getLogId();
        TriggerType getTriggerType();
        LocalDateTime getCreatedAt();
    }

    boolean existsByTrigger_Id(Long triggerId);
    Optional<Decision> findByTrigger_Id(Long triggerId);

    @Query("""
        select d
        from Decision d
        join fetch d.trigger t
        join fetch t.category c
        join fetch t.plan p
        where d.id = :decisionId
    """)
    Optional<Decision> findByIdWithTriggerPlanCategory(Long decisionId);

    @Query("""
        select
            d.id as logId,
            t.triggerType as triggerType,
            d.decidedAt as createdAt
        from Decision d
        join d.trigger t
        join t.plan p
        where p.id = :planId
          and p.ownerId = :ownerId
          and d.decisionType = :decisionType
        order by d.decidedAt desc
    """)
    List<KeepDecisionActivityRow> findRecentKeepDecisionActivities(
            @Param("ownerId") Long ownerId,
            @Param("planId") Long planId,
            @Param("decisionType") DecisionType decisionType,
            Pageable pageable
    );
}
