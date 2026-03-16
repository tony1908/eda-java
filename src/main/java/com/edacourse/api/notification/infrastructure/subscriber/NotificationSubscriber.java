package com.edacourse.api.notification.infrastructure.subscriber;

import com.edacourse.api.notification.application.service.NotificationService;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;

public class NotificationSubscriber {
    private final NotificationService notificationService;

    public NotificationSubscriber(EventBus eventBus, NotificationService notificationService) {
        this.notificationService = notificationService;
        eventBus.subscribe("inventory.reserved", InventoryReservedData.class, this::onInventoryReserved, "notification");
        eventBus.subscribe("inventory.insufficient", InventoryInsufficientData.class, this::onInventoryInsufficient, "notification");
        eventBus.subscribe("payment.completed", PaymentCompletedData.class, this::onPaymentCompleted, "notification");
        eventBus.subscribe("payment.failed", PaymentFailedData.class, this::onPaymentFailed, "notification");
        eventBus.subscribe("shipping.shipped", ShippingData.class, this::onOrderShipped, "notification");
        eventBus.subscribe("inventory.stock-low", StockLowData.class, this::onStockLow, "notification");
    }

    private void onInventoryReserved(InventoryReservedData event) {
        notificationService.notify("STOCK_RESERVADO", event.orderId,
            "Producto: " + event.productId + " x" + event.quantity);
    }

    private void onInventoryInsufficient(InventoryInsufficientData event) {
        notificationService.notify("STOCK_INSUFICIENTE", event.orderId,
            "Producto: " + event.productId + " (solicitado: " + event.requestedQuantity + ", disponible: " + event.availableStock + ")");
    }

    private void onPaymentCompleted(PaymentCompletedData event) {
        notificationService.notify("PAGO_COMPLETADO", event.orderId,
            "Monto: $" + event.amount);
    }

    private void onPaymentFailed(PaymentFailedData event) {
        notificationService.notify("PAGO_FALLIDO", event.orderId,
            "Monto: $" + event.amount + " — Razón: " + event.reason);
    }

    private void onOrderShipped(ShippingData event) {
        notificationService.notify("ORDEN_ENVIADA", event.orderId,
            "Tracking: " + event.trackingNumber);
    }

    private void onStockLow(StockLowData event) {
        notificationService.notify("STOCK_BAJO", "-",
            "Producto: " + event.productId + " (stock: " + event.currentStock + ")");
    }

    // Local DTOs to avoid cross-context imports
    public static class InventoryReservedData {
        public String orderId;
        public String productId;
        public int quantity;
    }

    public static class InventoryInsufficientData {
        public String orderId;
        public String productId;
        public int requestedQuantity;
        public int availableStock;
    }

    public static class PaymentCompletedData {
        public String paymentId;
        public String orderId;
        public double amount;
    }

    public static class PaymentFailedData {
        public String orderId;
        public double amount;
        public String reason;
    }

    public static class ShippingData {
        public String shipmentId;
        public String orderId;
        public String trackingNumber;
    }

    public static class StockLowData {
        public String productId;
        public int currentStock;
    }
}
