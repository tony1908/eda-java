package com.edacourse.api.shipping.infrastructure.subscriber;

import com.edacourse.api.shipping.application.service.ShippingService;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;

public class ShippingSubscriber {
    private final ShippingService shippingService;

    public ShippingSubscriber(EventBus eventBus, ShippingService shippingService) {
        this.shippingService = shippingService;
        eventBus.subscribe("payment.completed", PaymentCompletedData.class, this::onPaymentCompleted, "shipping");
    }

    private void onPaymentCompleted(PaymentCompletedData event) {
        shippingService.createShipment(event.orderId);
    }

    public static class PaymentCompletedData {
        public String paymentId;
        public String orderId;
        public double amount;
    }
}
