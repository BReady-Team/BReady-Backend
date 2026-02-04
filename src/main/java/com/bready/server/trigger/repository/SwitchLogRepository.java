package com.bready.server.trigger.repository;

import com.bready.server.trigger.domain.SwitchLog;
import com.bready.server.trigger.domain.TriggerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SwitchLogRepository extends JpaRepository<SwitchLog, Long> {
    interface SwitchActivityRow {
        Long getLogId();
        TriggerType getTriggerType();
        LocalDateTime getCreatedAt();
    }

    @Query("""
        select
            sl.id as logId,
            t.triggerType as triggerType,
            sl.createdAt as createdAt
        from SwitchLog sl
        join sl.decision d
        join d.trigger t
        join t.plan p
        where p.id = :planId
          and p.ownerId = :ownerId
        order by sl.createdAt desc
    """)
    Page<SwitchActivityRow> findRecentSwitchActivities(
            @Param("ownerId") Long ownerId,
            @Param("planId") Long planId,
            Pageable pageable
    );

    @Query("""
        select count(sl)
        from SwitchLog sl
        join sl.decision d
        join d.trigger t
        join t.plan p
        where p.ownerId = :ownerId
          and (:startAt is null or sl.createdAt >= :startAt)
    """)
    long countByOwnerIdAndPeriod(@Param("ownerId")Long ownerId, @Param("startAt")LocalDateTime startAt);

    boolean existsByDecision_Id(Long decisionId);
}
