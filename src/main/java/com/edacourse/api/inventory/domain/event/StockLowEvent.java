package com.edacourse.api.inventory.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record StockLowEvent(
    String productId,
    int currentStock
) implements DomainEvent {}
