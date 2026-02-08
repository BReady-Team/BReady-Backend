package com.bready.server.plan.repository;

import com.bready.server.plan.domain.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Page<Plan> findAllByOwnerIdAndDeletedAtIsNull(Long ownerId, Pageable pageable);

}
