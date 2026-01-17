package com.bready.server.plan.repository;

import com.bready.server.plan.domain.PlanCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanCategoryRepository extends JpaRepository<PlanCategory, Long> {
}
