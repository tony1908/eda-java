package com.edacourse.api.catalog.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record ProductChangedEvent(
    String productId,
    String name,
    String description,
    double price,
    String category,
    int stock,
    String operation
) implements DomainEvent {}
