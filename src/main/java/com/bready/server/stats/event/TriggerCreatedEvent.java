package com.bready.server.stats.event;

import java.time.LocalDateTime;

public record TriggerCreatedEvent(Long planId, LocalDateTime occurredAt) {
}