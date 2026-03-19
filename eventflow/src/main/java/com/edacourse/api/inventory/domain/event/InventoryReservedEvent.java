package com.edacourse.api.inventory.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record InventoryReservedEvent(
    String orderId,
    String productId,
    int quantity
) implements DomainEvent {}
