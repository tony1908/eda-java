package com.edacourse.api.service;

import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.domain.event.PaymentCompletedEvent;

public class PaymentService {
    private final EventBus eventBus;

    public PaymentService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void processPayment(String product, double price) {
        System.out.println("Procesando pago para: " + product + ", precio: " + price);
        eventBus.publish("payment.completed", new PaymentCompletedEvent(product, price));
    }
}
