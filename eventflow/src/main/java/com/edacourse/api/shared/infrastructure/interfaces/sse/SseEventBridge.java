package com.edacourse.api.shared.infrastructure.interfaces.sse;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.shared.infrastructure.serialization.EventSerializer;
import com.edacourse.api.shared.infrastructure.sse.EventSseBroadcaster;

public class SseEventBridge {
    private final EventSseBroadcaster broadcaster;
    private final EventSerializer serializer;

    public SseEventBridge(EventBus eventBus, EventSseBroadcaster broadcaster, EventSerializer serializer) {
        this.broadcaster = broadcaster;
        this.serializer = serializer;

        eventBus.subscribe("orders.created", Object.class,
            e -> broadcast("order.created", "orders", e), "sse-bridge-all");
        eventBus.subscribe("orders.canceled", Object.class,
            e -> broadcast("order.canceled", "orders", e), "sse-bridge-all");
    }

    private void broadcast(String eventType, String topic, Object event) {
        String json = serializer.serialize(event);
        broadcaster.broadcast(eventType, topic, json);
    }
    
}
