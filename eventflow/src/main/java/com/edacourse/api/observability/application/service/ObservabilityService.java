package com.edacourse.api.observability.application.service;

import com.edacourse.api.observability.domain.model.EventMetrics;
import com.edacourse.api.observability.domain.model.EventTrace;
import com.edacourse.api.observability.infrastructure.subscriber.MetricsCollectorSubscriber;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio de observabilidad que expone metricas y trazas.
 */
public class ObservabilityService {

    private final MetricsCollectorSubscriber metricsCollector;

    public ObservabilityService(MetricsCollectorSubscriber metricsCollector) {
        this.metricsCollector = metricsCollector;
        System.out.println("[OBSERVABILITY] Servicio inicializado");
    }

    /**
     * Dashboard JSON con todas las metricas.
     */
    public String getDashboard() {
        EventMetrics m = metricsCollector.getMetrics();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"uptime_seconds\":").append((Instant.now().toEpochMilli() - m.getStartedAt().toEpochMilli()) / 1000);
        sb.append(",\"total_published\":").append(m.getTotalPublished());
        sb.append(",\"total_consumed\":").append(m.getTotalConsumed());
        sb.append(",\"total_errors\":").append(m.getTotalErrors());
        sb.append(",\"throughput_per_second\":").append(String.format("%.2f", m.getThroughput()));
        sb.append(",\"error_rate\":").append(m.getTotalConsumed() > 0 ? String.format("%.4f", (double) m.getTotalErrors() / m.getTotalConsumed()) : "0");

        // Per-topic metrics
        sb.append(",\"topics\":{");
        boolean first = true;
        for (Map.Entry<String, AtomicLong> entry : m.getConsumedByTopic().entrySet()) {
            if (!first) sb.append(",");
            first = false;
            String topic = entry.getKey();
            sb.append("\"").append(topic).append("\":{");
            sb.append("\"consumed\":").append(entry.getValue().get());
            AtomicLong published = m.getPublishedByTopic().get(topic);
            sb.append(",\"published\":").append(published != null ? published.get() : 0);
            AtomicLong errors = m.getErrorsByTopic().get(topic);
            sb.append(",\"errors\":").append(errors != null ? errors.get() : 0);
            sb.append(",\"avg_latency_ms\":").append(String.format("%.1f", m.getAverageLatency(topic)));
            sb.append("}");
        }
        sb.append("}");

        sb.append(",\"started_at\":\"").append(m.getStartedAt()).append("\"");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Trazas recientes.
     */
    public String getRecentTraces(int limit) {
        List<EventTrace> traces = metricsCollector.getRecentTraces();
        int start = Math.max(0, traces.size() - limit);
        List<EventTrace> recent = traces.subList(start, traces.size());

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < recent.size(); i++) {
            if (i > 0) sb.append(",");
            EventTrace t = recent.get(i);
            sb.append(String.format("{\"correlationId\":\"%s\",\"causationId\":\"%s\",\"eventType\":\"%s\",\"topic\":\"%s\",\"service\":\"%s\",\"latencyMs\":%d,\"timestamp\":\"%s\"}",
                t.correlationId(), t.causationId() != null ? t.causationId() : "null",
                t.eventType(), t.topic(), t.service(), t.latencyMs(), t.timestamp()));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Trazas por correlationId — rastrear un flujo completo.
     */
    public String getTracesByCorrelation(String correlationId) {
        List<EventTrace> traces = metricsCollector.getTracesByCorrelation(correlationId);
        StringBuilder sb = new StringBuilder("{\"correlationId\":\"").append(correlationId).append("\",\"events\":[");
        for (int i = 0; i < traces.size(); i++) {
            if (i > 0) sb.append(",");
            EventTrace t = traces.get(i);
            sb.append(String.format("{\"eventType\":\"%s\",\"topic\":\"%s\",\"service\":\"%s\",\"latencyMs\":%d,\"timestamp\":\"%s\"}",
                t.eventType(), t.topic(), t.service(), t.latencyMs(), t.timestamp()));
        }
        sb.append("],\"total_events\":").append(traces.size()).append("}");
        return sb.toString();
    }
}
