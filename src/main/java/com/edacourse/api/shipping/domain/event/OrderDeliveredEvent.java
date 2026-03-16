package com.edacourse.api.shipping.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record OrderDeliveredEvent(
    String shipmentId,
    String orderId
) implements DomainEvent {}
