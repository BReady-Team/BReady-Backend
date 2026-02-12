package com.bready.server.stats.listener;

import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.event.SwitchLogCreatedEvent;
import com.bready.server.stats.event.TriggerCreatedEvent;
import com.bready.server.stats.service.PlanStatsUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PlanStatsEventListener {

    private final PlanStatsUpdater updater;

    // Switch 발생 → switchCount 변경
    @Async
    @TransactionalEventListener
    public void handleSwitchCreated(
            SwitchLogCreatedEvent event
    ) {
        Long planId = event.planId();

        updater.recalculate(planId, StatsPeriod.WEEK);
        updater.recalculate(planId, StatsPeriod.MONTH);
        updater.recalculate(planId, StatsPeriod.ALL);
    }

    // Trigger 발생 → triggerCount 변경
    @Async
    @TransactionalEventListener
    public void handleTriggerCreated(
            TriggerCreatedEvent event
    ) {
        Long planId = event.planId();

        updater.recalculate(planId, StatsPeriod.WEEK);
        updater.recalculate(planId, StatsPeriod.MONTH);
        updater.recalculate(planId, StatsPeriod.ALL);
    }
}
