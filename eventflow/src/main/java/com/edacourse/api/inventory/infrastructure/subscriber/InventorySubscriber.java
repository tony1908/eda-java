package com.edacourse.api.inventory.infrastructure.subscriber;

import com.edacourse.api.inventory.application.service.InventoryService;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;

import java.util.List;
import java.util.Map;

public class InventorySubscriber {
    private final InventoryService inventoryService;

    public InventorySubscriber(EventBus eventBus, InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        eventBus.subscribe("orders.created", OrderCreatedData.class, this::onOrderCreated, "inventory");
        eventBus.subscribe("orders.canceled", OrderCanceledData.class, this::onOrderCanceled, "inventory");
    }

    private void onOrderCreated(OrderCreatedData event) {
        for (var item : event.items) {
            inventoryService.reserveStock(event.orderId, item.productId, item.quantity);
        }
    }

    private void onOrderCanceled(OrderCanceledData event) {
        inventoryService.releaseStock(event.orderId);
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

    public static class OrderCanceledData {
        public String orderId;
        public String reason;
    }
}
