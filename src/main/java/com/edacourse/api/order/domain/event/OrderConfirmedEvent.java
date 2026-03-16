package com.edacourse.api.order.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record OrderConfirmedEvent(
    String orderId
) implements DomainEvent {}
