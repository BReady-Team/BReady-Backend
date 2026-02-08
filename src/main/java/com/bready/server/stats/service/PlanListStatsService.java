package com.bready.server.stats.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.plan.repository.PlanRepository;
import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.dto.PlanStatsItem;
import com.bready.server.stats.dto.PlanStatsResponse;
import com.bready.server.stats.exception.StatsErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanListStatsService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final PlanRepository planRepository;
    private final PlanCategoryRepository categoryRepository;

    public PlanStatsResponse getPlanStats(
            Long ownerId,
            StatsPeriod period,
            Integer limitParam
    ) {

        int limit = normalizeLimit(limitParam);

        Pageable pageable = PageRequest.of(0, limit);

        List<PlanRepository.PlanSwitchStatsRow> rows = planRepository.findPlanSwitchStats(ownerId, pageable);

        Map<Long, List<String>> categoryMap =
                categoryRepository.findCategoryTypesByOwner(ownerId)
                        .stream()
                        .collect(Collectors.groupingBy(
                                PlanCategoryRepository.PlanCategoryTypeRow::getPlanId,
                                Collectors.mapping(
                                        PlanCategoryRepository.PlanCategoryTypeRow::getCategoryType,
                                        Collectors.toList()
                                )
                        ));

        List<PlanStatsItem> items = rows.stream()
                .map(row -> PlanStatsItem.builder()
                        .planId(row.getPlanId())
                        .planTitle(row.getPlanTitle())
                        .planDate(row.getPlanDate())
                        .region(row.getRegion())
                        .categoryTypes(
                                categoryMap.getOrDefault(
                                        row.getPlanId(),
                                        List.of()
                                )
                        )
                        .totalSwitches(row.getTotalSwitches())
                        .build())
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