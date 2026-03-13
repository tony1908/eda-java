package com.edacourse.api.subscriber;

import com.edacourse.api.infrastructure.messaging.RoutableSubscriber;
import com.edacourse.api.domain.event.InventoryReservedEvent;
import com.edacourse.api.domain.event.PaymentCompletedEvent;
import com.edacourse.api.service.NotificationService;

public class NotificationSubscriber {
    private final NotificationService notificationService;

    public NotificationSubscriber(RoutableSubscriber eventBus, NotificationService notificationService) {
        this.notificationService = notificationService;
        eventBus.subscribe("inventory.reserved", "inventory.*", InventoryReservedEvent.class, this::onInventoryReserved);
        eventBus.subscribe("payment.completed", "payment.*", PaymentCompletedEvent.class, this::onPaymentCompleted);
    }

    private void onInventoryReserved(InventoryReservedEvent event) {
        notificationService.notifyEvent("inventory.reserved",
            "producto: " + event.product() + ", cantidad: " + event.quantity());
    }

    private void onPaymentCompleted(PaymentCompletedEvent event) {
        notificationService.notifyEvent("payment.completed",
            "producto: " + event.product() + ", precio: " + event.price());
    }
}
