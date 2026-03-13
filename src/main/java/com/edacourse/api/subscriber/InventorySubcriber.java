package com.edacourse.api.subscriber;

import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.domain.event.OrderCreatedEvent;
import com.edacourse.api.domain.event.OrderCanceledEvent;

import com.edacourse.api.service.InventoryService;

public class InventorySubcriber {
    private final InventoryService inventoryService;

    public InventorySubcriber(EventBus eventBus, InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        eventBus.subscribe("orders.created", OrderCreatedEvent.class, this::onOrderCreated, "inventory");
        eventBus.subscribe("orders.canceled", OrderCanceledEvent.class, this::onOrderCanceled, "inventory");
    }

    private void onOrderCreated(OrderCreatedEvent event) {
        inventoryService.updateInventory("product", 1);
    }

    private void onOrderCanceled(OrderCanceledEvent event) {
        inventoryService.updateInventory("product", -1);
    }
}
