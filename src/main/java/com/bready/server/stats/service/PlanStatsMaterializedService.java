package com.bready.server.stats.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.repository.PlanRepository;
import com.bready.server.stats.domain.PlanStats;
import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.dto.PlanStatsItem;
import com.bready.server.stats.dto.PlanStatsResponse;
import com.bready.server.stats.exception.StatsErrorCase;
import com.bready.server.stats.repository.PlanStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanStatsMaterializedService {

    private final PlanStatsRepository planStatsRepository;
    private final PlanRepository planRepository;

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    public PlanStatsResponse getStats(
            Long ownerId,
            StatsPeriod period,
            Integer limit
    ) {
        int size = normalizeLimit(limit);

        Pageable pageable =
                PageRequest.of(0, size, Sort.by("planDate").descending());

        List<Plan> plans =
                planRepository.findAllByOwnerIdAndDeletedAtIsNull(ownerId, pageable)
                        .getContent();

        if (plans.isEmpty()) {
            return PlanStatsResponse.empty(period);
        }

        List<Long> planIds = plans.stream().map(Plan::getId).toList();

        Map<Long, PlanStats> statsMap =
                planStatsRepository
                        .findByPeriodAndPlanIdIn(period, planIds)
                        .stream()
                        .collect(Collectors.toMap(
                                PlanStats::getPlanId,
                                s -> s
                        ));

        List<PlanStatsItem> items =
                plans.stream()
                        .map(plan -> {

                            PlanStats stats = statsMap.get(plan.getId());

                            return PlanStatsItem.builder()
                                    .planId(plan.getId())
                                    .planTitle(plan.getTitle())
                                    .planDate(plan.getPlanDate())
                                    .region(plan.getRegion())
                                    .totalSwitches(
                                            stats == null
                                                    ? 0L
                                                    : stats.getTotalSwitches()
                                    )
                                    .build();
                        })
                        .toList();

        return PlanStatsResponse.builder()
                .period(period)
                .items(items)
                .build();
    }


    private int normalizeLimit(Integer limitParam) {
        int limit = limitParam == null ? DEFAULT_LIMIT : limitParam;
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw ApplicationException.from(StatsErrorCase.INVALID_PARAMETER);
        }
        return limit;
    }
}