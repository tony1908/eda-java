package com.edacourse.api.saga.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;
import java.time.Instant;

public record SagaStepCompletedEvent(
    String sagaId,
    String orderId,
    String step,
    String status,
    Instant completedAt
) implements DomainEvent {}
