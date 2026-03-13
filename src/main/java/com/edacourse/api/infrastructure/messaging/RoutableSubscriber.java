package com.edacourse.api.infrastructure.messaging;

public interface RoutableSubscriber extends EventSubscriber {
    <T> void subscribe(String topic, String routingKeyOrPattern, Class<T> eventType, EventHandler<T> handler);
}
