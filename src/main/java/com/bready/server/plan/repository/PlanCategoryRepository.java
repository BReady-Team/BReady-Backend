package com.bready.server.plan.repository;

import com.bready.server.plan.domain.PlanCategory;
import org.springframework.data.domain.Pageable;
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

    @Query("""
    select pc
    from PlanCategory pc
    where pc.plan.id = :planId
      and pc.deletedAt is null
    order by pc.sequence desc
""")
    List<PlanCategory> findLastByPlanId(@Param("planId") Long planId, Pageable pageable);


    // planIds로만 category 조회 (성능 개선)
    @Query("""
        select
            pc.plan.id as planId,
            pc.categoryType as categoryType
        from PlanCategory pc
        where pc.plan.id in :planIds
    """)
    List<PlanCategoryTypeRow> findCategoryTypesByPlanIds(@Param("planIds") List<Long> planIds);

    // 장소 후보쪽에서 category가 plan에 속하는지 검증하기 위해서 추가
    Optional<PlanCategory> findByIdAndPlan_Id(Long id, Long planId);

    // 플랜 상세 조회 - 카테고리 + 후보 + place
    @Query("""
    select distinct pc
    from PlanCategory pc
    left join fetch pc.candidates cand
    left join fetch cand.place
    where pc.plan.id = :planId
      and pc.deletedAt is null
    order by pc.sequence asc
""")
    List<PlanCategory> findAllDetailByPlanId(@Param("planId") Long planId);
}
