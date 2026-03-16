package com.edacourse.api.payment.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record PaymentCompletedEvent(
    String paymentId,
    String orderId,
    double amount
) implements DomainEvent {}
