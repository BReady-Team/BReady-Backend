package com.bready.server.stats.domain;

import com.bready.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "plan_stats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_plan_stats_plan_period",
                        columnNames = {"plan_id", "period"}
                )
        }
)
public class PlanStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatsPeriod period;

    @Column(name = "total_triggers")
    private Integer totalTriggers;

    @Column(name = "total_switches")
    private Integer totalSwitches;

    @Column(name = "reliability_score", precision = 5, scale = 2)
    private BigDecimal reliabilityScore;

    public static PlanStats create(
            Long planId,
            StatsPeriod period,
            int totalTriggers,
            int totalSwitches,
            BigDecimal reliabilityScore
    ) {
        PlanStats stats = new PlanStats();
        stats.planId = planId;
        stats.period = period;
        stats.totalTriggers = totalTriggers;
        stats.totalSwitches = totalSwitches;
        stats.reliabilityScore = reliabilityScore;
        return stats;
    }

    public void update(
            int totalTriggers,
            int totalSwitches,
            BigDecimal reliabilityScore
    ) {
        this.totalTriggers = totalTriggers;
        this.totalSwitches = totalSwitches;
        this.reliabilityScore = reliabilityScore;
    }
}