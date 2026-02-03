package com.bready.server.stats.service;

import com.bready.server.plan.repository.PlanRepository;
import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.dto.StatsSummaryResponse;
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
@Transactional(readOnly = true)
public class StatsService {

    private final PlanRepository planRepository;
    private final SwitchLogRepository switchLogRepository;
    private final TriggerRepository triggerRepository;

    public StatsSummaryResponse getSummary(Long ownerId, StatsPeriod period) {

        LocalDateTime startAt = resolveStartAt(period);

        long totalPlans = planRepository.countByOwnerId(ownerId);
        long totalSwitches = switchLogRepository.countByOwnerIdAndPeriod(ownerId, startAt);
        long recentCount = triggerRepository.countByOwnerIdAndPeriod(ownerId, startAt);

        double avgSwitchesPerPlan = calculateAvg(totalSwitches, totalPlans);

        return StatsSummaryResponse.builder()
                .period(period)
                .totalPlans(totalPlans)
                .totalSwitches(totalSwitches)
                .avgSwitchesPerPlan(avgSwitchesPerPlan)
                .recentCount(recentCount)
                .build();
    }

    private LocalDateTime resolveStartAt(StatsPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case WEEK -> now.minusWeeks(1);
            case MONTH -> now.minusMonths(1);
            case ALL -> null;
        };
    }

    private double calculateAvg(long totalSwitches, long totalPlans) {
        if (totalPlans == 0) return 0.0;
        // 소수점 1자리 반올림
        return BigDecimal.valueOf((double) totalSwitches / totalPlans)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
