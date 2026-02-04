package com.bready.server.plan.repository;

import com.bready.server.plan.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
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
}
