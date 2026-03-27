package com.edacourse.api.observability.domain.model;

import java.util.UUID;

/**
 * Contexto de correlacion para rastrear un flujo de eventos.
 * Se propaga a traves de toda la cadena de eventos de un pedido.
 * Usa ThreadLocal para mantener el contexto en el hilo actual.
 */
public class CorrelationContext {

    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();
    private static final ThreadLocal<String> causationId = new ThreadLocal<>();
    private static final ThreadLocal<String> originService = new ThreadLocal<>();

    public static String getCorrelationId() {
        String id = correlationId.get();
        if (id == null) {
            id = "corr-" + UUID.randomUUID().toString().substring(0, 8);
            correlationId.set(id);
        }
        return id;
    }

    public static void setCorrelationId(String id) {
        correlationId.set(id);
    }

    public static String getCausationId() {
        return causationId.get();
    }

    public static void setCausationId(String id) {
        causationId.set(id);
    }

    public static String getOriginService() {
        return originService.get() != null ? originService.get() : "unknown";
    }

    public static void setOriginService(String service) {
        originService.set(service);
    }

    public static void clear() {
        correlationId.remove();
        causationId.remove();
        originService.remove();
    }

    /**
     * Genera un nuevo contexto de correlacion para un flujo nuevo.
     */
    public static String newContext(String service) {
        String id = "corr-" + UUID.randomUUID().toString().substring(0, 8);
        correlationId.set(id);
        causationId.set(id); // First event is its own cause
        originService.set(service);
        return id;
    }
}
