package com.edacourse.api.infrastructure.messaging;

public interface EventPublisher {
    void publish(String topic, Object event);
    void publish(String topic, Object event, String partitionKey);
}
