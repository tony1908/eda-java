package com.edacourse.api.saga.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;
import java.time.Instant;
import java.util.List;

public record SagaFailedEvent(
    String sagaId,
    String orderId,
    String failedStep,
    String reason,
    List<String> compensatedSteps,
    Instant failedAt
) implements DomainEvent {}
