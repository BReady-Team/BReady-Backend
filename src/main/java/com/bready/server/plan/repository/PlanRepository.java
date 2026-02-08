package com.bready.server.plan.repository;

import com.bready.server.plan.domain.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
        select
            p.id as planId,
            p.title as planTitle,
            p.planDate as planDate,
            p.region as region,
            count(sl.id) as totalSwitches
        from Plan p
        left join Trigger t on t.plan = p
        left join Decision d on d.trigger = t
        left join SwitchLog sl on sl.decision = d
        where p.ownerId = :ownerId
        group by p.id, p.title, p.planDate, p.region
        order by p.planDate desc
    """)
    List<PlanSwitchStatsRow> findPlanSwitchStats(
            @Param("ownerId") Long ownerId,
            Pageable pageable
    );

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
    """)
    long countByOwnerId(@Param("ownerId") Long ownerId);

    Optional<Plan> findByIdAndDeletedAtIsNull(Long id);

}
