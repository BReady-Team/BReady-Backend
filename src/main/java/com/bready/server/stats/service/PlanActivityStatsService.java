package com.bready.server.stats.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.repository.PlanRepository;
import com.bready.server.stats.dto.PlanActivitiesResponse;
import com.bready.server.stats.dto.PlanActivityItem;
import com.bready.server.stats.exception.StatsErrorCase;
import com.bready.server.trigger.domain.DecisionType;
import com.bready.server.trigger.repository.DecisionRepository;
import com.bready.server.trigger.repository.SwitchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanActivityStatsService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    private final PlanRepository planRepository;
    private final SwitchLogRepository switchLogRepository;
    private final DecisionRepository decisionRepository;

    public PlanActivitiesResponse getPlanActivities(Long ownerId, Long planId, Integer limitParam) {

        int limit = normalizeLimit(limitParam);

        Plan plan = planRepository.findByIdAndOwnerId(planId, ownerId)
                .orElseThrow(() -> ApplicationException.from(StatsErrorCase.INVALID_PARAMETER));

        PageRequest pageable = PageRequest.of(0, limit);

        List<SwitchLogRepository.SwitchActivityRow> switchRows =
                switchLogRepository.findRecentSwitchActivities(ownerId, planId, pageable);

        List<DecisionRepository.KeepDecisionActivityRow> keepRows =
                decisionRepository.findRecentKeepDecisionActivities(ownerId, planId, DecisionType.KEEP, pageable);
        List<PlanActivityItem> merged = new ArrayList<>();

        for (SwitchLogRepository.SwitchActivityRow row : switchRows) {
            String activityId = "SWITCH-" + row.getLogId();

            merged.add(PlanActivityItem.builder()
                    .activityId(activityId)
                    .triggerType(row.getTriggerType())
                    .decisionType(DecisionType.SWITCH)
                    .createdAt(row.getCreatedAt())
                    .build());
        }

        for (DecisionRepository.KeepDecisionActivityRow row : keepRows) {
            String activityId = "KEEP-" + row.getLogId();

            merged.add(PlanActivityItem.builder()
                    .activityId(activityId)
                    .triggerType(row.getTriggerType())
                    .decisionType(DecisionType.KEEP)
                    .createdAt(row.getCreatedAt())
                    .build());
        }

        List<PlanActivityItem> items = merged.stream()
                .sorted(Comparator.comparing(PlanActivityItem::createdAt).reversed())
                .limit(limit)
                .toList();

        return PlanActivitiesResponse.builder()
                .planId(plan.getId())
                .planTitle(plan.getTitle())
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
}