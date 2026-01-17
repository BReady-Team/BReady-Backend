package com.bready.server.plan.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
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
}
