package com.edacourse.api.payment.application.service;

import com.edacourse.api.payment.domain.model.Payment;
import com.edacourse.api.payment.domain.repository.PaymentRepository;
import com.edacourse.api.payment.domain.event.PaymentCompletedEvent;
import com.edacourse.api.payment.domain.event.PaymentFailedEvent;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;

public class PaymentService {
    private final EventBus eventBus;
    private final PaymentRepository repository;

    public PaymentService(EventBus eventBus, PaymentRepository repository) {
        this.eventBus = eventBus;
        this.repository = repository;
    }

    public void processPayment(String orderId, double amount) {
        Payment payment = new Payment(orderId, amount);
        repository.save(payment);

        payment.complete();
        repository.save(payment);

        System.out.println("Pago procesado: " + payment.getId() + " para orden " + orderId + " por $" + amount);
        eventBus.publish("payment.completed",
            new PaymentCompletedEvent(payment.getId(), orderId, amount),
            orderId);
    }
}
