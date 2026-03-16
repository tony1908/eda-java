package com.edacourse.api.payment.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record PaymentFailedEvent(
    String orderId,
    double amount,
    String reason
) implements DomainEvent {}
