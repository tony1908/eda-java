package com.edacourse.solid;

public interface EventBus extends AutoCloseable {
    void publish(String topic, Object event);
    <T> void subscribe(String topic, Class<T> eventType, EventHandler<T> handler);
    void close();
}
