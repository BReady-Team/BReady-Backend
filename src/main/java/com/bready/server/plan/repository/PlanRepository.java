package com.bready.server.plan.repository;

import com.bready.server.plan.domain.Plan;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Page<Plan> findAllByOwnerIdAndDeletedAtIsNull(Long ownerId, Pageable pageable);

    interface PlanSwitchStatsRow {
        Long getPlanId();

        String getPlanTitle();

        LocalDate getPlanDate();

        String getRegion();

        Long getTotalSwitches();
    }

    @Query("""
                select p
                from Plan p
                where p.id = :planId
                  and p.ownerId = :ownerId
            """)
    Optional<Plan> findByIdAndOwnerId(@Param("planId") Long planId, @Param("ownerId") Long ownerId);

    @Query("""
        select count(p)
            from Plan p
        where p.ownerId = :ownerId
            and p.deletedAt is null
    """)
    long countActiveByOwnerId(@Param("ownerId") Long ownerId);

    // 조회용 - 락 없음
    Optional<Plan> findByIdAndDeletedAtIsNull(Long id);

    @Query(value = """
    SELECT
        p.id as planId,
        p.title as planTitle,
        p.plan_date as planDate,
        p.region as region,
        COALESCE(COUNT(sl.id),0) as totalSwitches
    FROM (
        SELECT id, title, plan_date, region
        FROM plans
        WHERE owner_id = :ownerId
            AND deleted_at IS NULL
        ORDER BY plan_date DESC
        LIMIT :limit
    ) p
    LEFT JOIN triggers t ON t.plan_id = p.id AND t.deleted_at IS NULL
    LEFT JOIN decisions d ON d.trigger_id = t.id AND d.deleted_at IS NULL
    LEFT JOIN switch_logs sl ON sl.decision_id = d.id AND sl.deleted_at IS NULL
    GROUP BY p.id, p.title, p.plan_date, p.region
    ORDER BY p.plan_date DESC
    """, nativeQuery = true)
    List<PlanSwitchStatsRow> findPlanSwitchStatsOptimized(
            @Param("ownerId") Long ownerId,
            @Param("limit") int limit
    );

    // 락 전용 - 카테고리 추가(addCategory)에서만 사용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select p
    from Plan p
    where p.id = :id
      and p.deletedAt is null
""")
    Optional<Plan> findByIdAndDeletedAtIsNullForUpdate(@Param("id") Long id);
}
