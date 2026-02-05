package com.bready.server.plan.repository;

import com.bready.server.plan.domain.PlanCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlanCategoryRepository extends JpaRepository<PlanCategory, Long> {
    interface PlanCategoryTypeRow {
        Long getPlanId();
        String getCategoryType();
    }

    @Query("""
        select
            pc.plan.id as planId,
            pc.categoryType as categoryType
        from PlanCategory pc
        join pc.plan p
        where p.ownerId = :ownerId
    """)
    List<PlanCategoryTypeRow> findCategoryTypesByOwner(@Param("ownerId") Long ownerId);

    // 장소 후보쪽에서 category가 plan에 속하는지 검증하기 위해서 추가
    Optional<PlanCategory> findByIdAndPlan_Id(Long id, Long planId);
}
