package com.bready.server.plan.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "category_selection_logs")
public class CategorySelectionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "selected_at", nullable = false)
    private LocalDateTime selectedAt;

    public static CategorySelectionLog of(
            Long categoryId,
            Long candidateId,
            LocalDateTime selectedAt
    ) {
        CategorySelectionLog log = new CategorySelectionLog();
        log.categoryId = categoryId;
        log.candidateId = candidateId;
        log.selectedAt = selectedAt;
        return log;
    }
}
