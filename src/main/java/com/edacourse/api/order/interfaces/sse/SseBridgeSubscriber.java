package com.edacourse.api.order.interfaces.sse;

import com.edacourse.api.order.domain.event.OrderCreatedEvent;
import com.edacourse.api.order.domain.event.OrderCanceledEvent;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.shared.infrastructure.serialization.EventSerializer;

public class SseBridgeSubscriber {
    private final OrderSseResource sseResource;
    private final EventSerializer serializer;

    public SseBridgeSubscriber(EventBus eventBus, EventSerializer serializer, OrderSseResource sseResource) {
        this.sseResource = sseResource;
        this.serializer = serializer;
        eventBus.subscribe("orders.created", OrderCreatedEvent.class, this::onOrderCreated, "sse-bridge");
        eventBus.subscribe("orders.canceled", OrderCanceledEvent.class, this::onOrderCanceled, "sse-bridge");
    }

    private void onOrderCreated(OrderCreatedEvent event) {
        sseResource.broadcast("order.created", serializer.serialize(event));
    }

    private void onOrderCanceled(OrderCanceledEvent event) {
        sseResource.broadcast("order.canceled", serializer.serialize(event));
    }
}
