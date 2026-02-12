package com.bready.server.stats.service;

import com.bready.server.stats.domain.PlanStats;
import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.repository.PlanStatsRepository;
import com.bready.server.trigger.repository.SwitchLogRepository;
import com.bready.server.trigger.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanStatsUpdater {

    private final PlanStatsRepository planStatsRepository;
    private final TriggerRepository triggerRepository;
    private final SwitchLogRepository switchLogRepository;

    public void recalculate(Long planId, StatsPeriod period) {

        LocalDateTime from = resolveFrom(period);

        long triggerCount =
                triggerRepository.countByPlanIdAndPeriod(planId, from);

        long switchCount =
                switchLogRepository.countSwitchByPlanIdAndPeriod(planId, from);

        BigDecimal reliability =
                calculateReliability(triggerCount, switchCount);

        PlanStats stats = planStatsRepository
                .findByPlanIdAndPeriod(planId, period)
                .orElseGet(() ->
                        PlanStats.create(
                                planId,
                                period,
                                0,
                                0,
                                BigDecimal.ZERO
                        )
                );

        stats.update(
                (int) triggerCount,
                (int) switchCount,
                reliability
        );

        planStatsRepository.save(stats);
    }

    private LocalDateTime resolveFrom(StatsPeriod period) {
        LocalDateTime now = LocalDateTime.now();

        return switch (period) {
            case WEEK -> now.minusWeeks(1);
            case MONTH -> now.minusMonths(1);
            case ALL -> LocalDateTime.MIN;
        };
    }

    private BigDecimal calculateReliability(
            long triggerCount,
            long switchCount
    ) {
        if (triggerCount == 0) {
            return BigDecimal.valueOf(100);
        }

        double ratio = 1 - ((double) switchCount / triggerCount);

        return BigDecimal.valueOf(ratio * 100)
                .setScale(2, RoundingMode.HALF_UP);
    }
}

