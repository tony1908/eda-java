package com.edacourse.api.infrastructure.messaging;

public interface AdvancedEventBus extends AutoCloseable, RoutablePublisher, RoutableSubscriber, DeadLetterHandler {
    void close();
}
