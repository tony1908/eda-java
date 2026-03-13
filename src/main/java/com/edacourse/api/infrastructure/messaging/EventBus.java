package com.edacourse.api.infrastructure.messaging;

public interface EventBus extends AutoCloseable, EventPublisher, EventSubscriber {
    void close();
}
