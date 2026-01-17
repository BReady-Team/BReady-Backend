package com.bready.server.plan.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "category_states",
        uniqueConstraints = {@UniqueConstraint(columnNames = "category_id")}
)
public class CategoryState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "current_candidate_id")
    private Long currentCandidateId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
