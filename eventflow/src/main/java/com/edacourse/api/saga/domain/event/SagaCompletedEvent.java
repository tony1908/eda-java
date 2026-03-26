package com.edacourse.api.saga.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;
import java.time.Instant;

public record SagaCompletedEvent(
    String sagaId,
    String orderId,
    long durationMs,
    Instant completedAt
) implements DomainEvent {}
