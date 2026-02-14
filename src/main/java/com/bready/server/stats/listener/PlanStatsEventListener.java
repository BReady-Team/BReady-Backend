package com.bready.server.stats.listener;

import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.event.SwitchLogCreatedEvent;
import com.bready.server.stats.event.TriggerCreatedEvent;
import com.bready.server.stats.service.PlanStatsUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanStatsEventListener {

    private final PlanStatsUpdater updater;

    // Switch 발생 → switchCount 변경
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSwitchCreated(SwitchLogCreatedEvent event) {
        recalculateAll(event.planId());
    }

    // Trigger 발생 → triggerCount 변경
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTriggerCreated(TriggerCreatedEvent event) {
        recalculateAll(event.planId());
    }

    private void recalculateAll(Long planId) {
        for (StatsPeriod period : StatsPeriod.values()) {
            try {
                updater.recalculate(planId, period);
            } catch (Exception e) {
                log.error("PlanStats 갱신 실패: planId={}, period={}", planId, period, e);
            }
        }
    }
}
