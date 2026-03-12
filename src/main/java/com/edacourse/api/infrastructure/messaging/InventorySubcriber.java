package com.edacourse.api.infrastructure.messaging;

import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.infrastructure.messaging.OrderEvent;

public class InventorySubcriber {
    public InventorySubcriber(EventBus eventBus) {
        eventBus.subscribe("orders", OrderEvent.class, this::onOrderCreated);
    }

    private void onOrderCreated(OrderEvent event) {
        System.out.println("Llego el evento al InventorySubcriber");
    }
}
