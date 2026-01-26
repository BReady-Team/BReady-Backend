package com.bready.server.trigger.repository;

import com.bready.server.trigger.domain.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
    boolean existsByTrigger_Id(Long triggerId);
    Optional<Decision> findByTrigger_Id(Long triggerId);

    @Query("""
        select d
        from Decision d
        join fetch d.trigger t
        join fetch t.category c
        join fetch t.plan p
        where d.id = :decisionId
    """)
    Optional<Decision> findByIdWithTriggerPlanCategory(Long decisionId);
}
