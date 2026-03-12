package com.edacourse.api.service;

import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.domain.event.InventoryReservedEvent;

public class InventoryService {
    private final EventBus eventBus;

    public InventoryService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void updateInventory(String product, int quantity) {
        System.out.println("Actualizando inventario para: " + product + ", cantidad: " + quantity);
        eventBus.publish("inventory.reserved", new InventoryReservedEvent(product, quantity));
    }
}
