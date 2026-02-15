package com.bready.server.stats.event;

import java.util.Objects;

public record SwitchLogCreatedEvent(
        Long planId
) {
    public SwitchLogCreatedEvent {
                Objects.requireNonNull(planId, "planId는 null이 될 수 없습니다.");
    }
}
