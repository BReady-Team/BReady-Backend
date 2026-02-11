package com.bready.server.stats.service;

import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.dto.PlanStatsResponse;
import com.bready.server.stats.repository.PlanStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanStatsMaterializedService {

    private final PlanStatsRepository planStatsRepository;

    public PlanStatsResponse getStats(
            Long ownerId,
            StatsPeriod period,
            Integer limit
    ) {

        // 아직 구현 안함 (JOIN 성능 먼저 측정해야 비교 가능.)
        throw new UnsupportedOperationException(
                "Materialized stats not implemented yet"
        );
    }
}