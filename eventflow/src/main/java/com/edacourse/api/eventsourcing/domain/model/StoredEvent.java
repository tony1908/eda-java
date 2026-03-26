package com.edacourse.api.eventsourcing.domain.model;

import java.time.Instant;

/**
 * Evento almacenado en el Event Store.
 * Representa un hecho inmutable que ocurrio en el sistema.
 */
public record StoredEvent(
    long globalPosition,
    String streamId,
    String aggregateType,
    String eventType,
    String payload,
    Instant occurredAt
) {}
