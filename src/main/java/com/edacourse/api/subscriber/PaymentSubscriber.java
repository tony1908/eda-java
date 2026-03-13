package com.edacourse.api.subscriber;

import com.edacourse.api.infrastructure.messaging.RoutableSubscriber;
import com.edacourse.api.domain.event.OrderCreatedEvent;
import com.edacourse.api.service.PaymentService;

public class PaymentSubscriber {
    private final PaymentService paymentService;

    public PaymentSubscriber(RoutableSubscriber eventBus, PaymentService paymentService) {
        this.paymentService = paymentService;
        eventBus.subscribe("orders.created", "orders.created", OrderCreatedEvent.class, this::onOrderCreated);
    }

    private void onOrderCreated(OrderCreatedEvent event) {
        paymentService.processPayment(event.product(), event.price());
    }
}
