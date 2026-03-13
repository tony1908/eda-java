package com.edacourse.api.subscriber;

import com.edacourse.api.infrastructure.messaging.RoutableSubscriber;
import com.edacourse.api.domain.event.OrderCreatedEvent;
import com.edacourse.api.domain.event.OrderCanceledEvent;
import com.edacourse.api.domain.event.InventoryReservedEvent;

public class AnalyticsSubscriber {
    private final RoutableSubscriber eventBus;

    public AnalyticsSubscriber(RoutableSubscriber eventBus) {
        this.eventBus = eventBus;
        eventBus.subscribe("orders.created", "orders.#", OrderCreatedEvent.class, this::onAnalytics);
        eventBus.subscribe("orders.canceled", "orders.#", OrderCanceledEvent.class, this::onAnalytics);
        eventBus.subscribe("inventory.reserved", "inventory.#", InventoryReservedEvent.class, this::onAnalytics);
    }

    private void onAnalytics(Object event) {
        System.out.println("Analizando evento: " + event);
    }
}
