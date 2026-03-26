package com.edacourse.api.eventsourcing.infrastructure.subscriber;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.shared.infrastructure.serialization.EventSerializer;
import com.edacourse.api.eventsourcing.infrastructure.persistence.SqlServerEventStore;

/**
 * Suscriptor que persiste TODOS los eventos del e-commerce en el Event Store.
 * Escucha los topics relevantes y los almacena como hechos inmutables.
 */
public class EventStoreSubscriber {

    private final SqlServerEventStore eventStore;
    private final EventSerializer serializer;

    public EventStoreSubscriber(EventBus eventBus, EventSerializer serializer, SqlServerEventStore eventStore) {
        this.eventStore = eventStore;
        this.serializer = serializer;

        // Order lifecycle events
        eventBus.subscribe("orders.created", Object.class,
            e -> store(e, "OrderCreated", "Order"), "event-store");
        eventBus.subscribe("orders.canceled", Object.class,
            e -> store(e, "OrderCanceled", "Order"), "event-store");

        // Inventory events
        eventBus.subscribe("inventory.reserved", Object.class,
            e -> store(e, "InventoryReserved", "Order"), "event-store");
        eventBus.subscribe("inventory.insufficient", Object.class,
            e -> store(e, "InventoryInsufficient", "Order"), "event-store");

        // Payment events
        eventBus.subscribe("payment.completed", Object.class,
            e -> store(e, "PaymentCompleted", "Order"), "event-store");
        eventBus.subscribe("payment.failed", Object.class,
            e -> store(e, "PaymentFailed", "Order"), "event-store");

        // Shipping events
        eventBus.subscribe("shipping.shipped", Object.class,
            e -> store(e, "OrderShipped", "Order"), "event-store");

        System.out.println("[EVENT-STORE-SUB] Suscrito a 7 topics para persistir en Event Store");
    }

    private void store(Object event, String eventType, String aggregateType) {
        String json = serializer.serialize(event);
        String aggregateId = extractOrderId(json);
        if (aggregateId != null) {
            eventStore.append(aggregateId, aggregateType, eventType, json);
            System.out.println("[EVENT-STORE-SUB] Persistido: " + eventType + " para " + aggregateId);
        }
    }

    private String extractOrderId(String json) {
        // Buscar orderId en el JSON
        int idx = json.indexOf("orderId");
        if (idx < 0) return null;
        int start = json.indexOf("\"", idx + 9);
        if (start < 0) return null;
        int end = json.indexOf("\"", start + 1);
        return end > start ? json.substring(start + 1, end) : null;
    }
}
