package com.edacourse.api.subscriber;

import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.domain.event.InventoryReservedEvent;
import com.edacourse.api.domain.event.PaymentCompletedEvent;
import com.edacourse.api.service.NotificationService;

public class NotificationSubscriber {
    private final NotificationService notificationService;

    public NotificationSubscriber(EventBus eventBus, NotificationService notificationService) {
        this.notificationService = notificationService;
        eventBus.subscribe("inventory.reserved", InventoryReservedEvent.class, this::onInventoryReserved, "notification");
        eventBus.subscribe("payment.completed", PaymentCompletedEvent.class, this::onPaymentCompleted, "notification");
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
