package com.edacourse.api.inventory.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record InventoryReleasedEvent(
    String orderId,
    String productId,
    int quantity
) implements DomainEvent {}
