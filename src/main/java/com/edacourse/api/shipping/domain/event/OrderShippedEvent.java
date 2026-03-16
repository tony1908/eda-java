package com.edacourse.api.shipping.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record OrderShippedEvent(
    String shipmentId,
    String orderId,
    String trackingNumber
) implements DomainEvent {}
