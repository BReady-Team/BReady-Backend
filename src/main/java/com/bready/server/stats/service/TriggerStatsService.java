package com.bready.server.stats.service;

import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.dto.TriggerStatsItem;
import com.bready.server.stats.dto.TriggerStatsResponse;
import com.bready.server.trigger.domain.TriggerType;
import com.bready.server.trigger.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TriggerStatsService {

    private final TriggerRepository triggerRepository;

    public TriggerStatsResponse getTriggerStats(Long userId, StatsPeriod period) {

        LocalDateTime startAt = resolveStartAt(period);

        long totalCount = triggerRepository.countByOwnerIdAndPeriod(userId, startAt);

        Map<TriggerType, Long> countMap = triggerRepository
                .countByTriggerType(userId, startAt)
                .stream()
                .collect(Collectors.toMap(
                        row -> (TriggerType) row[0],
                        row -> (Long) row[1]
                ));

        List<TriggerStatsItem> items = Arrays.stream(TriggerType.values())
                .map(type -> {
                    long count = countMap.getOrDefault(type, 0L);
                    int percentage = totalCount == 0
                            ? 0
                            : (int) Math.round((double) count * 100 / totalCount);

                    return TriggerStatsItem.builder()
                            .triggerType(type)
                            .count(count)
                            .percentage(percentage)
                            .build();
                })
                .toList();

        return TriggerStatsResponse.builder()
                .period(period)
                .totalCount(totalCount)
                .items(items)
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
}
