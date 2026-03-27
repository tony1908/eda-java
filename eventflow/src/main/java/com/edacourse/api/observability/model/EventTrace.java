package com.edacourse.api.observability.domain.model;

import java.time.Instant;

/**
 * Traza de un evento individual en el flujo.
 */
public record EventTrace(
    String correlationId,
    String causationId,
    String eventType,
    String topic,
    String service,
    long latencyMs,
    Instant timestamp
) {}
