package com.bready.server.trigger.repository;

import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.domain.TriggerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TriggerRepository extends JpaRepository<Trigger, Long> {
    interface TriggerTypeCount {
        TriggerType getTriggerType();
        long getCount();
    }
    // 기간 내 전체 트리거 수 (요약)
    @Query("""
        select count(t)
        from Trigger t
        join t.plan p
        where p.ownerId = :ownerId
          and (:startAt is null or t.occurredAt >= :startAt)
    """)
    long countByOwnerIdAndPeriod(@Param("ownerId") Long ownerId, @Param("startAt") LocalDateTime startAt);

    // 트리거 타입별 발생 수 (분석 통계)
    @Query("""
        select t.triggerType, count(t)
        from Trigger t
        join t.plan p
        where p.ownerId = :ownerId
          and (:startAt is null or t.occurredAt >= :startAt)
        group by t.triggerType
    """)
    List<TriggerTypeCount> countByTriggerType(@Param("ownerId") Long ownerId, @Param("startAt") LocalDateTime startAt);
}
