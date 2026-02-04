package com.bready.server.plan.repository;

import com.bready.server.plan.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    long countByOwnerId(Long ownerId);
}
