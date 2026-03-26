package com.edacourse.api.cqrs.domain.model;

import java.time.Instant;

/**
 * Modelo de lectura desnormalizado para consultas rapidas de pedidos.
 * Se actualiza via proyeccion de eventos — NO via escritura directa.
 */
public record OrderReadModel(
    String orderId,
    String customerId,
    String status,
    String statusLabel,
    double totalAmount,
    String trackingNumber,
    int itemCount,
    String itemsSummary,
    Instant createdAt,
    Instant lastUpdatedAt,
    int eventCount
) {}
