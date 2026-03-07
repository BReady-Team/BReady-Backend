package com.bready.server.stats.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.dto.RecentActivitiesResponse;
import com.bready.server.stats.dto.RecentActivityItem;
import com.bready.server.stats.exception.StatsErrorCase;
import com.bready.server.trigger.domain.DecisionType;
import com.bready.server.trigger.repository.DecisionRepository;
import com.bready.server.trigger.repository.SwitchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentActivityStatsService {

    private static final int DEFAULT_LIMIT = 3;
    private static final int MAX_LIMIT = 20;

    private final SwitchLogRepository switchLogRepository;
    private final DecisionRepository decisionRepository;

    public RecentActivitiesResponse getRecentActivities(
            Long ownerId,
            StatsPeriod period,
            Integer limitParam
    ) {
        int limit = normalizeLimit(limitParam);
        LocalDateTime startAt = resolveStartAt(period);

        PageRequest pageable = PageRequest.of(0, limit * 2);

        List<SwitchLogRepository.RecentSwitchActivityRow> switchRows =
                switchLogRepository.findRecentSwitchActivitiesAllPlans(ownerId, startAt, pageable);

        List<DecisionRepository.RecentKeepActivityRow> keepRows =
                decisionRepository.findRecentKeepActivities(ownerId, startAt, pageable);

        List<RecentActivityItem> merged = new ArrayList<>();

        for (SwitchLogRepository.RecentSwitchActivityRow row : switchRows) {
            merged.add(
                    RecentActivityItem.builder()
                            .activityId("SWITCH-" + row.getLogId())
                            .planId(row.getPlanId())
                            .planTitle(row.getPlanTitle())
                            .triggerType(row.getTriggerType())
                            .decisionType(DecisionType.SWITCH)
                            .createdAt(row.getCreatedAt())
                            .build()
            );
        }

        for (DecisionRepository.RecentKeepActivityRow row : keepRows) {
            merged.add(
                    RecentActivityItem.builder()
                            .activityId("KEEP-" + row.getDecisionId())
                            .planId(row.getPlanId())
                            .planTitle(row.getPlanTitle())
                            .triggerType(row.getTriggerType())
                            .decisionType(DecisionType.KEEP)
                            .createdAt(row.getCreatedAt())
                            .build()
            );
        }

        List<RecentActivityItem> items = merged.stream()
                .sorted(Comparator.comparing(RecentActivityItem::createdAt).reversed())
                .limit(limit)
                .toList();

        return RecentActivitiesResponse.builder()
                .period(period)
                .limit(limit)
                .items(items)
                .build();
    }

    private int normalizeLimit(Integer limitParam) {
        int limit = (limitParam == null) ? DEFAULT_LIMIT : limitParam;
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw ApplicationException.from(StatsErrorCase.INVALID_LIMIT);
        }
        return limit;
    }

    private LocalDateTime resolveStartAt(StatsPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case WEEK -> now.minusWeeks(1);
            case MONTH -> now.minusMonths(1);
            case ALL -> null;
        };
    }
}
