package com.edacourse.api.infrastructure.messaging;

@FunctionalInterface
public interface EventHandler<T> {
    void handle(T event);
}
