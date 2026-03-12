package com.edacourse.api.infrastructure.messaging;

public interface EventBus extends AutoCloseable {
    void publish(String topic, Object event);
    void publish(String topic, Object event, String partitionKey);
    <T> void subscribe(String topic, Class<T> eventType, EventHandler<T> handler);
    void close();
}
