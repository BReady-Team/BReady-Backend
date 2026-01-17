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

    @Column(name = "current_candidate_id")
    private Long currentCandidateId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, unique = true)
    private PlanCategory category; // PlanCategory와의 연관관계 설정 (1:1)
}
