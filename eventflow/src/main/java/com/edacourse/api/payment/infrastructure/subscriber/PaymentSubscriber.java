package com.edacourse.api.payment.infrastructure.subscriber;

import com.edacourse.api.payment.application.service.PaymentService;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;

import java.util.List;

public class PaymentSubscriber {
    private final PaymentService paymentService;

    public PaymentSubscriber(EventBus eventBus, PaymentService paymentService) {
        this.paymentService = paymentService;
        eventBus.subscribe("orders.created", OrderCreatedData.class, this::onOrderCreated, "payment");
    }

    private void onOrderCreated(OrderCreatedData event) {
        paymentService.processPayment(event.orderId, event.totalAmount);
    }

    public static class OrderCreatedData {
        public String orderId;
        public String customerId;
        public List<ItemData> items;
        public double totalAmount;

        public static class ItemData {
            public String productId;
            public String productName;
            public double price;
            public int quantity;
        }
    }
}
