package com.bready.server.plan.repository;

import com.bready.server.plan.domain.CategoryState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryStateRepository extends JpaRepository<CategoryState, Long> {
    Optional<CategoryState> findByCategory_Id(Long categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 비관적 락 적용 (동시성 제어)
    @Query("""
        select cs
        from CategoryState cs
        where cs.category.id = :categoryId
    """)
    Optional<CategoryState> findByCategory_IdForUpdate(
            @Param("categoryId") Long categoryId
    );

    // 플랜 상세 조회 - 대표 후보 상태 일괄 조회
    List<CategoryState> findAllByCategory_IdIn(List<Long> categoryIds);
}
