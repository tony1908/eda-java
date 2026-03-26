package com.edacourse.api.eventsourcing.domain.model;

import java.time.Instant;

/**
 * Snapshot del estado de un pedido en un punto especifico.
 * Optimizacion: en vez de replay desde el evento 1,
 * se carga el snapshot + se aplican eventos posteriores.
 */
public record OrderSnapshot(
    String orderId,
    String status,
    String customerId,
    double totalAmount,
    String trackingNumber,
    int version,
    Instant snapshotAt
) {}
