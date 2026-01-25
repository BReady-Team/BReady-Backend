package com.bready.server.trigger.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TriggerCreateResponse(
        Long triggerId,
        LocalDateTime occurredAt
) {
}
