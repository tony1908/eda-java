package com.edacourse.api.observability.infrastructure.subscriber;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.observability.domain.model.EventMetrics;
import com.edacourse.api.observability.domain.model.EventTrace;
import com.edacourse.api.observability.domain.model.CorrelationContext;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Suscriptor que recolecta metricas de TODOS los eventos del sistema.
 * Se suscribe a todos los topics relevantes para contar, medir latencia,
 * y mantener un historial de trazas.
 */
public class MetricsCollectorSubscriber {

    private final EventMetrics metrics;
    private final CopyOnWriteArrayList<EventTrace> recentTraces = new CopyOnWriteArrayList<>();
    private static final int MAX_TRACES = 200;

    private static final String[] MONITORED_TOPICS = {
        "orders.created", "orders.canceled",
        "inventory.reserved", "inventory.insufficient",
        "payment.completed", "payment.failed",
        "shipping.shipped",
        "backup.requested", "backup.completed", "backup.failed",
        "restore.requested", "restore.completed",
        "file.upload.started", "catalog.import.completed", "catalog.import.failed",
        "saga.step.completed", "saga.completed", "saga.failed"
    };

    public MetricsCollectorSubscriber(EventBus eventBus, EventMetrics metrics) {
        this.metrics = metrics;

        for (String topic : MONITORED_TOPICS) {
            final String t = topic;
            eventBus.subscribe(topic, Object.class, event -> {
                long start = System.currentTimeMillis();
                metrics.recordConsumed(t);

                // Record trace
                EventTrace trace = new EventTrace(
                    CorrelationContext.getCorrelationId(),
                    CorrelationContext.getCausationId(),
                    topicToEventType(t),
                    t,
                    CorrelationContext.getOriginService(),
                    0, // latency measured on consume
                    Instant.now()
                );
                addTrace(trace);
            }, "metrics-collector");
        }

        System.out.println("[OBSERVABILITY] Recolector de metricas activo — " + MONITORED_TOPICS.length + " topics monitoreados");
    }

    public EventMetrics getMetrics() {
        return metrics;
    }

    public List<EventTrace> getRecentTraces() {
        return List.copyOf(recentTraces);
    }

    public List<EventTrace> getTracesByCorrelation(String correlationId) {
        return recentTraces.stream()
            .filter(t -> correlationId.equals(t.correlationId()))
            .toList();
    }

    private void addTrace(EventTrace trace) {
        recentTraces.add(trace);
        // Trim to keep only recent traces
        while (recentTraces.size() > MAX_TRACES) {
            recentTraces.remove(0);
        }
    }

    private String topicToEventType(String topic) {
        return switch (topic) {
            case "orders.created" -> "OrderCreated";
            case "orders.canceled" -> "OrderCanceled";
            case "inventory.reserved" -> "InventoryReserved";
            case "inventory.insufficient" -> "InventoryInsufficient";
            case "payment.completed" -> "PaymentCompleted";
            case "payment.failed" -> "PaymentFailed";
            case "shipping.shipped" -> "OrderShipped";
            case "backup.completed" -> "BackupCompleted";
            case "saga.completed" -> "SagaCompleted";
            case "saga.failed" -> "SagaFailed";
            default -> topic;
        };
    }
}
