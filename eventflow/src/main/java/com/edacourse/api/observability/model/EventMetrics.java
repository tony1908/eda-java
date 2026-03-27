package com.edacourse.api.observability.domain.model;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

/**
 * Metricas de eventos en memoria.
 * Cuenta eventos publicados, consumidos, errores y latencia por topic.
 */
public class EventMetrics {

    private final AtomicLong totalPublished = new AtomicLong();
    private final AtomicLong totalConsumed = new AtomicLong();
    private final AtomicLong totalErrors = new AtomicLong();
    private final ConcurrentHashMap<String, AtomicLong> publishedByTopic = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> consumedByTopic = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> errorsByTopic = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalLatencyByTopic = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> latencyCountByTopic = new ConcurrentHashMap<>();
    private final Instant startedAt = Instant.now();

    public void recordPublished(String topic) {
        totalPublished.incrementAndGet();
        publishedByTopic.computeIfAbsent(topic, k -> new AtomicLong()).incrementAndGet();
    }

    public void recordConsumed(String topic) {
        totalConsumed.incrementAndGet();
        consumedByTopic.computeIfAbsent(topic, k -> new AtomicLong()).incrementAndGet();
    }

    public void recordError(String topic) {
        totalErrors.incrementAndGet();
        errorsByTopic.computeIfAbsent(topic, k -> new AtomicLong()).incrementAndGet();
    }

    public void recordLatency(String topic, long latencyMs) {
        totalLatencyByTopic.computeIfAbsent(topic, k -> new AtomicLong()).addAndGet(latencyMs);
        latencyCountByTopic.computeIfAbsent(topic, k -> new AtomicLong()).incrementAndGet();
    }

    public long getTotalPublished() { return totalPublished.get(); }
    public long getTotalConsumed() { return totalConsumed.get(); }
    public long getTotalErrors() { return totalErrors.get(); }
    public Map<String, AtomicLong> getPublishedByTopic() { return publishedByTopic; }
    public Map<String, AtomicLong> getConsumedByTopic() { return consumedByTopic; }
    public Map<String, AtomicLong> getErrorsByTopic() { return errorsByTopic; }
    public Instant getStartedAt() { return startedAt; }

    public double getAverageLatency(String topic) {
        AtomicLong total = totalLatencyByTopic.get(topic);
        AtomicLong count = latencyCountByTopic.get(topic);
        if (total == null || count == null || count.get() == 0) return 0.0;
        return (double) total.get() / count.get();
    }

    /**
     * Throughput: events per second since startup.
     */
    public double getThroughput() {
        long elapsed = Instant.now().toEpochMilli() - startedAt.toEpochMilli();
        if (elapsed <= 0) return 0;
        return (totalPublished.get() * 1000.0) / elapsed;
    }
}
