package com.edacourse.api.shared.infrastructure.messaging;

import com.edacourse.api.shared.infrastructure.serialization.EventSerializer;

public class EventBusFactory {
    public static EventBus create(EventSerializer serializer) {
        String broker = System.getenv().getOrDefault("BROKER", "memory");
        return switch (broker.toLowerCase()) {
            case "kafka" -> new KafkaEventBus(serializer);
            case "rabbitmq" -> new RabbitMQEventBus(serializer);
            default -> new InMemoryEventBus(serializer);
        };
    }
}
