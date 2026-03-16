package com.edacourse.api.shared.infrastructure.messaging;

public interface DeadLetterHandler {
    <T> void onDeadLetter(String topic, Class<T> eventType, EventHandler<T> handler);
}
