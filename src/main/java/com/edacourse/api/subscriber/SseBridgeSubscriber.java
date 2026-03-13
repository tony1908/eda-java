package com.edacourse.api.subscriber;

import com.edacourse.api.resource.OrderSseResource;
import com.edacourse.api.infrastructure.messaging.EventSerializer;
import com.edacourse.api.domain.event.OrderCreatedEvent;
import com.edacourse.api.domain.event.OrderCanceledEvent;
import com.edacourse.api.infrastructure.messaging.EventBus;

public class SseBridgeSubscriber {
    private final OrderSseResource sseResource;
    private final EventSerializer serializer;
    private final EventBus eventBus;

    public SseBridgeSubscriber(EventBus eventBus, EventSerializer serializer, OrderSseResource sseResource) {
        this.sseResource = sseResource;
        this.serializer = serializer;
        this.eventBus = eventBus;
        eventBus.subscribe("orders.created", OrderCreatedEvent.class, this::onOrderCreated, "sse");
        eventBus.subscribe("orders.canceled", OrderCanceledEvent.class, this::onOrderCanceled, "sse");
    }

    private void onOrderCreated(OrderCreatedEvent event) {
        sseResource.broadcast("order.created", serializer.serialize(event));
    }

    private void onOrderCanceled(OrderCanceledEvent event) {
        sseResource.broadcast("order.canceled", serializer.serialize(event));
    }
}
