package com.edacourse.api.saga.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;
import java.time.Instant;

/**
 * Evento de compensacion: inventario liberado tras fallo en el checkout.
 */
public record InventoryReleasedEvent(
    String sagaId,
    String orderId,
    Instant releasedAt
) implements DomainEvent {}
