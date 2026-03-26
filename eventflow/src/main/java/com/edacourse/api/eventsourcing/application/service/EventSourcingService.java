package com.edacourse.api.eventsourcing.application.service;

import com.edacourse.api.eventsourcing.domain.model.StoredEvent;
import com.edacourse.api.eventsourcing.domain.model.OrderState;
import com.edacourse.api.eventsourcing.domain.model.OrderSnapshot;
import com.edacourse.api.eventsourcing.infrastructure.persistence.SqlServerEventStore;

import java.time.Instant;
import java.util.List;

/**
 * Servicio de Event Sourcing para pedidos.
 * Reconstruye el estado de un pedido a partir de sus eventos.
 */
public class EventSourcingService {

    private final SqlServerEventStore eventStore;
    private static final int SNAPSHOT_THRESHOLD = 10; // Crear snapshot cada 10 eventos

    public EventSourcingService(SqlServerEventStore eventStore) {
        this.eventStore = eventStore;
        System.out.println("[EVENT-SOURCING] Servicio inicializado");
    }

    /**
     * Reconstruye el estado de un pedido desde sus eventos.
     * Usa snapshot si existe, luego aplica eventos posteriores.
     */
    public OrderState reconstruct(String orderId) {
        OrderState state = new OrderState();

        // Intentar cargar snapshot
        OrderSnapshot snapshot = eventStore.loadSnapshot(orderId);
        List<StoredEvent> events;

        if (snapshot != null) {
            // Aplicar snapshot como estado base
            applySnapshot(state, snapshot);
            // Solo cargar eventos posteriores al snapshot
            events = eventStore.getEventsAfter(orderId, findSequenceForVersion(orderId, snapshot.version()));
            System.out.println("[EVENT-SOURCING] Reconstruyendo " + orderId + " desde snapshot (v" + snapshot.version() + ") + " + events.size() + " eventos");
        } else {
            // Replay completo desde el inicio
            events = eventStore.getEvents(orderId);
            System.out.println("[EVENT-SOURCING] Reconstruyendo " + orderId + " desde " + events.size() + " eventos (sin snapshot)");
        }

        // Aplicar eventos
        for (StoredEvent event : events) {
            state.apply(event);
        }

        // Crear snapshot si hay suficientes eventos
        if (state.getVersion() >= SNAPSHOT_THRESHOLD && (snapshot == null || state.getVersion() - snapshot.version() >= SNAPSHOT_THRESHOLD)) {
            OrderSnapshot newSnapshot = new OrderSnapshot(
                orderId, state.getStatus(), state.getCustomerId(),
                state.getTotalAmount(), state.getTrackingNumber(),
                state.getVersion(), Instant.now()
            );
            eventStore.saveSnapshot(newSnapshot);
            System.out.println("[EVENT-SOURCING] Snapshot creado para " + orderId + " en version " + state.getVersion());
        }

        return state;
    }

    /**
     * Obtiene el historial completo de eventos de un pedido.
     */
    public List<StoredEvent> getHistory(String orderId) {
        return eventStore.getEvents(orderId);
    }

    /**
     * Lista todos los aggregate IDs de tipo Order.
     */
    public List<String> listOrderIds() {
        return eventStore.getAllEvents("Order").stream()
            .map(StoredEvent::streamId)
            .distinct()
            .toList();
    }

    private void applySnapshot(OrderState state, OrderSnapshot snapshot) {
        // Simular aplicacion de snapshot como si fueran eventos
        // El estado se inicializa desde el snapshot
        StoredEvent fakeEvent = new StoredEvent(
            0, snapshot.orderId(), "Order", "OrderCreated",
            "{\"orderId\":\"" + snapshot.orderId() + "\",\"customerId\":\"" + snapshot.customerId()
            + "\",\"totalAmount\":" + snapshot.totalAmount() + "}",
            snapshot.snapshotAt()
        );
        state.apply(fakeEvent);

        // Ajustar status al del snapshot
        if (!"CREATED".equals(snapshot.status())) {
            String statusEvent = switch (snapshot.status()) {
                case "INVENTORY_RESERVED" -> "InventoryReserved";
                case "PAID" -> "PaymentCompleted";
                case "SHIPPED" -> "OrderShipped";
                case "DELIVERED" -> "OrderDelivered";
                case "CANCELLED" -> "OrderCanceled";
                default -> null;
            };
            if (statusEvent != null) {
                String payload = snapshot.trackingNumber() != null
                    ? "{\"trackingNumber\":\"" + snapshot.trackingNumber() + "\"}"
                    : "{}";
                state.apply(new StoredEvent(0, snapshot.orderId(), "Order", statusEvent, payload, snapshot.snapshotAt()));
            }
        }
    }

    private long findSequenceForVersion(String orderId, int version) {
        List<StoredEvent> allEvents = eventStore.getEvents(orderId);
        if (version <= 0 || version > allEvents.size()) return 0;
        return allEvents.get(version - 1).globalPosition();
    }
}
