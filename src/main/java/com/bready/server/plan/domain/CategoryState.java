package com.bready.server.plan.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public static CategoryState create(PlanCategory category, Long representativeCandidateId) {
        CategoryState state = new CategoryState();
        state.category = category;
        state.currentCandidateId = representativeCandidateId;
        state.updatedAt = LocalDateTime.now();
        return state;
    }

    public void changeRepresentative(Long representativeCandidateId) {
        this.currentCandidateId = representativeCandidateId;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isRepresentative(Long candidateId) {
        return this.currentCandidateId != null && this.currentCandidateId.equals(candidateId);
    }
}
