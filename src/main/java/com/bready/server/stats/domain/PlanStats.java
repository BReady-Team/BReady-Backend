package com.bready.server.stats.domain;

import com.bready.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "plan_stats")
public class PlanStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(nullable = false)
    private String period;

    @Column(name = "total_triggers")
    private Integer totalTriggers;

    @Column(name = "total_switches")
    private Integer totalSwitches;

    @Column(name = "reliability_score")
    private Double reliabilityScore;
}
