package com.bready.server.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanStatsService {

    // TODO(stats): PlanStats 도메인/테이블 구현 후 planId 기준 total_triggers++ 반영 필요
    public void increaseTriggerCount(Long planId) {
        // 현재는 통계 미구현
    }
}
