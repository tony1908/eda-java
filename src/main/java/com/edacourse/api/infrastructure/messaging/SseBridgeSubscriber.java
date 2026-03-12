package com.edacourse.api.infrastructure.messaging;

import com.edacourse.api.resource.OrderSseResource;
import com.edacourse.api.infrastructure.messaging.EventSerializer;
import com.edacourse.api.infrastructure.messaging.OrderCreatedEvent;
import com.edacourse.api.infrastructure.messaging.OrderCanceledEvent;

public class SseBridgeSubscriber {
    private final OrderSseResource sseResource;
    private final EventSerializer serializer;

    public SseBridgeSubscriber(EventBus eventBus, EventSerializer serializer, OrderSseResource sseResource) {
        this.sseResource = sseResource;
        this.serializer = serializer;
        eventBus.subscribe("orders.created", OrderCreatedEvent.class, this::onOrderCreated);
        eventBus.subscribe("orders.canceled", OrderCanceledEvent.class, this::onOrderCanceled);
    }

    private void onOrderCreated(OrderCreatedEvent event) {
        sseResource.broadcast("order.created", serializer.serialize(event));
    }

    private void onOrderCanceled(OrderCanceledEvent event) {
        sseResource.broadcast("order.canceled", serializer.serialize(event));
    }
}
