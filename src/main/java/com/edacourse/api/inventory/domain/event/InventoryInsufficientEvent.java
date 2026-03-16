package com.edacourse.api.inventory.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record InventoryInsufficientEvent(
    String orderId,
    String productId,
    int requestedQuantity,
    int availableStock
) implements DomainEvent {}
