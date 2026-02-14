package com.bready.server.stats.event;

import java.time.LocalDateTime;
import java.util.Objects;

public record TriggerCreatedEvent(Long planId, LocalDateTime occurredAt) {
    public TriggerCreatedEvent {
        Objects.requireNonNull(planId, "planId는 null이 될 수 없습니다.");
        Objects.requireNonNull(occurredAt, "occurredAt는 null이 될 수 없습니다.");
    }
}