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

    Optional<PlanCategory> findByIdAndPlan_IdAndDeletedAtIsNull(Long id, Long planId);

    // 플랜 상세 조회에서 categories 조회용 (soft delete 차단 + sequence 정렬)
    List<PlanCategory> findAllByPlan_IdAndDeletedAtIsNullOrderBySequenceAsc(Long planId);

    // sequence 자동 부여용 max sequence 조회
    @Query("""
    select coalesce(max(pc.sequence), 0)
    from PlanCategory pc
    where pc.plan.id = :planId
      and pc.deletedAt is null
""")
    Integer findMaxSequenceByPlanId(@Param("planId") Long planId);

}
