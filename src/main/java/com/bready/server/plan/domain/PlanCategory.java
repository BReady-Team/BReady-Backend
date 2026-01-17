package com.bready.server.plan.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "plan_categories")
public class PlanCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "category_type", nullable = false)
    private String categoryType;

    @Column(nullable = false)
    private Integer sequence;
}
