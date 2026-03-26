package com.edacourse.api.saga.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;
import java.time.Instant;

/**
 * Evento de compensacion: pago reembolsado tras fallo en el checkout.
 */
public record PaymentRefundedEvent(
    String sagaId,
    String orderId,
    double amount,
    Instant refundedAt
) implements DomainEvent {}
