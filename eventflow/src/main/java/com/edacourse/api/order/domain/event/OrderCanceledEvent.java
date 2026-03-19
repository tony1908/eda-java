package com.edacourse.api.order.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record OrderCanceledEvent(
    String orderId,
    String reason
) implements DomainEvent {}
