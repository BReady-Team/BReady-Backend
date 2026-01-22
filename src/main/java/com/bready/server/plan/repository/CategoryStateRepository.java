package com.bready.server.plan.repository;

import com.bready.server.plan.domain.CategoryState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryStateRepository extends JpaRepository<CategoryState, Long> {
    Optional<CategoryState> findByCategory_Id(Long categoryId);
}
