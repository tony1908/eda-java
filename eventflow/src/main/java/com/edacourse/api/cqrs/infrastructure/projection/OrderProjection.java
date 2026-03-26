package com.edacourse.api.cqrs.infrastructure.projection;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.cqrs.infrastructure.persistence.OrderReadModelRepository;

/**
 * Proyeccion que escucha eventos del dominio y actualiza el modelo de lectura.
 * Esta es la pieza central de CQRS: el puente entre escritura y lectura.
 */
public class OrderProjection {

    private final OrderReadModelRepository readRepo;

    public OrderProjection(EventBus eventBus, OrderReadModelRepository readRepo) {
        this.readRepo = readRepo;

        eventBus.subscribe("orders.created", Object.class,
            this::onOrderCreated, "cqrs-projection");

        eventBus.subscribe("inventory.reserved", Object.class,
            e -> updateFromEvent(e, "INVENTORY_RESERVED", "Inventario Reservado"), "cqrs-projection");

        eventBus.subscribe("inventory.insufficient", Object.class,
            e -> updateFromEvent(e, "INVENTORY_FAILED", "Sin Stock"), "cqrs-projection");

        eventBus.subscribe("payment.completed", Object.class,
            e -> updateFromEvent(e, "PAID", "Pago Completado"), "cqrs-projection");

        eventBus.subscribe("payment.failed", Object.class,
            e -> updateFromEvent(e, "PAYMENT_FAILED", "Pago Fallido"), "cqrs-projection");

        eventBus.subscribe("shipping.shipped", Object.class,
            this::onOrderShipped, "cqrs-projection");

        eventBus.subscribe("orders.canceled", Object.class,
            e -> updateFromEvent(e, "CANCELLED", "Cancelado"), "cqrs-projection");

        System.out.println("[CQRS-PROJECTION] Proyeccion de pedidos activa — 7 topics suscritos");
    }

    private void onOrderCreated(Object event) {
        System.out.println("[CQRS-PROJECTION] Evento recibido: " + event);
        String json = event.toString();
        String orderId = extractField(json, "orderId");
        String customerId = extractField(json, "customerId");
        double totalAmount = extractDouble(json, "totalAmount");

        System.out.println("[CQRS-PROJECTION] Evento recibido: " + json);
        System.out.println("[CQRS-PROJECTION] orderId: " + orderId);
        

        // Extraer items summary
        int itemCount = countItems(json);
        String itemsSummary = extractItemsSummary(json);

        if (orderId != null) {
            System.out.println("[CQRS-PROJECTION] Insertando pedido: " + orderId);
            readRepo.insert(orderId, customerId, totalAmount, itemCount, itemsSummary);
        }
    }

    private void onOrderShipped(Object event) {
        System.out.println("[CQRS-PROJECTION] Evento recibido: " + event);
        String json = event.toString();
        String orderId = extractField(json, "orderId");
        String trackingNumber = extractField(json, "trackingNumber");

        if (orderId != null) {
            readRepo.updateStatus(orderId, "SHIPPED", "Enviado");
            if (trackingNumber != null) {
                readRepo.updateTracking(orderId, trackingNumber);
            }
        }
    }

    private void updateFromEvent(Object event, String status, String statusLabel) {
        String json = event.toString();
        String orderId = extractField(json, "orderId");
        if (orderId != null) {
            readRepo.updateStatus(orderId, status, statusLabel);
        }
    }

    private String extractField(String text, String field) {
        // Try JSON format: "field":"value"
        String jsonPattern = "\"" + field + "\"";
        int idx = text.indexOf(jsonPattern);
        if (idx >= 0) {
            int colonIdx = text.indexOf(":", idx);
            if (colonIdx >= 0) {
                int start = text.indexOf("\"", colonIdx + 1);
                if (start >= 0) {
                    int end = text.indexOf("\"", start + 1);
                    if (end > start) return text.substring(start + 1, end);
                }
            }
        }
        // Try Map.toString() format: field=value
        String mapPattern = field + "=";
        idx = text.indexOf(mapPattern);
        if (idx < 0) return null;
        int start = idx + mapPattern.length();
        int end = start;
        while (end < text.length() && text.charAt(end) != ',' && text.charAt(end) != '}' && text.charAt(end) != ']') {
            end++;
        }
        String value = text.substring(start, end).trim();
        return value.isEmpty() ? null : value;
    }

    private double extractDouble(String text, String field) {
        String value = extractField(text, field);
        if (value == null) return 0.0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int countItems(String json) {
        int count = 0;
        int idx = 0;
        while ((idx = json.indexOf("productId", idx)) >= 0) {
            count++;
            idx++;
        }
        return Math.max(count, 1);
    }

    private String extractItemsSummary(String json) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        while ((idx = json.indexOf("productName", idx)) >= 0) {
            int start = json.indexOf("\"", idx + 13);
            int end = json.indexOf("\"", start + 1);
            if (start >= 0 && end > start) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(json, start + 1, end);
            }
            idx = end > 0 ? end : idx + 1;
        }
        return sb.length() > 0 ? sb.toString() : "N/A";
    }
}
