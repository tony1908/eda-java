package com.edacourse.api.infrastructure.messaging;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class InMemoryEventBus implements EventBus {

    private final Map<String, List<Consumer<String>>> subscriptions = new ConcurrentHashMap<>();
    private final EventSerializer serializer;

    public InMemoryEventBus(EventSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void publish(String topic, Object event) {
        publish(topic, event, "");
    }

    @Override
    public void publish(String topic, Object event, String key) {
        String json = serializer.serialize(event);

        for (Consumer<String> subscriber : subscriptions.getOrDefault(topic, List.of())) {
            subscriber.accept(json);
        }
    }

    @Override
    public <T> void subscribe(String topic, Class<T> eventType, EventHandler<T> handler) {
        subscriptions.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>())
                .add(json -> {
                    T event = serializer.deserialize(json, eventType);
                    handler.handle(event);
                });
    }

    @Override
    public void close() {
        subscriptions.clear();
    }
}
