package com.edacourse.api.infrastructure.messaging;

import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.infrastructure.messaging.OrderCreatedEvent;
import com.edacourse.api.infrastructure.messaging.OrderCanceledEvent;

public class InventorySubcriber {
    public InventorySubcriber(EventBus eventBus) {
        eventBus.subscribe("orders.created", OrderCreatedEvent.class, this::onOrderCreated);
        eventBus.subscribe("orders.canceled", OrderCanceledEvent.class, this::onOrderCanceled);
    }

    private void onOrderCreated(OrderCreatedEvent event) {
        System.out.println("Llego el evento al InventorySubcriber");
    }

    private void onOrderCanceled(OrderCanceledEvent event) {
        System.out.println("Llego el evento al InventorySubcriber para cancelar el pedido");
    }
}
