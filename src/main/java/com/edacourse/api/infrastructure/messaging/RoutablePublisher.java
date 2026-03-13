package com.edacourse.api.infrastructure.messaging;

public interface RoutablePublisher extends EventPublisher {
    void publish(String topic, String routingKey, Object event);
}
