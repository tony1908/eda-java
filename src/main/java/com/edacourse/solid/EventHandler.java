package com.edacourse.solid;

@FunctionalInterface
public interface EventHandler<T> {
    void handle(T event);
}
