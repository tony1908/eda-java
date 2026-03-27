package com.edacourse.api.observability.infrastructure.logging;

import com.edacourse.api.observability.domain.model.CorrelationContext;

import java.time.Instant;

/**
 * Logger estructurado que incluye correlationId en cada mensaje.
 * Formato JSON-like para facilitar el parseo por herramientas de log.
 */
public class StructuredLogger {

    private final String serviceName;

    public StructuredLogger(String serviceName) {
        this.serviceName = serviceName;
    }

    public void info(String message) {
        log("INFO", message, null);
    }

    public void info(String message, String topic) {
        log("INFO", message, topic);
    }

    public void warn(String message) {
        log("WARN", message, null);
    }

    public void warn(String message, String topic) {
        log("WARN", message, topic);
    }

    public void error(String message) {
        log("ERROR", message, null);
    }

    public void error(String message, String topic) {
        log("ERROR", message, topic);
    }

    private void log(String level, String message, String topic) {
        String correlationId = CorrelationContext.getCorrelationId();
        String causationId = CorrelationContext.getCausationId();
        String topicField = topic != null ? ",\"topic\":\"" + topic + "\"" : "";

        System.out.printf("{\"timestamp\":\"%s\",\"level\":\"%s\",\"service\":\"%s\",\"correlationId\":\"%s\",\"causationId\":\"%s\"%s,\"message\":\"%s\"}%n",
            Instant.now(), level, serviceName, correlationId, causationId != null ? causationId : "null", topicField, message);
    }

    /**
     * Log an event with full context.
     */
    public void logEvent(String action, String eventType, String topic, String orderId) {
        String correlationId = CorrelationContext.getCorrelationId();
        System.out.printf("{\"timestamp\":\"%s\",\"level\":\"INFO\",\"service\":\"%s\",\"correlationId\":\"%s\",\"action\":\"%s\",\"eventType\":\"%s\",\"topic\":\"%s\",\"orderId\":\"%s\"}%n",
            Instant.now(), serviceName, correlationId, action, eventType, topic, orderId);
    }
}
