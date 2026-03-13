package com.edacourse.api.infrastructure.messaging;

public interface EventSubscriber {
    <T> void subscribe(String topic, Class<T> eventType, EventHandler<T> handler, String consumerGroup);
}
