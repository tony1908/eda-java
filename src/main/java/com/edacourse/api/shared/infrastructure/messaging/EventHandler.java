package com.edacourse.api.shared.infrastructure.messaging;

@FunctionalInterface
public interface EventHandler<T> {
    void handle(T event);
}
