package com.bready.server.trigger.repository;

import com.bready.server.trigger.domain.SwitchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SwitchLogRepository extends JpaRepository<SwitchLog, Long> {
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
