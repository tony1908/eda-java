package com.edacourse.api.eventsourcing.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

/**
 * Estado de un pedido reconstruido desde eventos.
 * No se persiste — se recalcula cada vez que se necesita.
 */
public class OrderState {
    private String orderId;
    private String customerId;
    private String status;
    private double totalAmount;
    private List<String> items = new ArrayList<>();
    private String cancelReason;
    private String trackingNumber;
    private int version;
    private Instant createdAt;
    private Instant lastUpdatedAt;

    public void apply(StoredEvent event) {
        version++;
        lastUpdatedAt = event.occurredAt();

        switch (event.eventType()) {
            case "OrderCreated" -> {
                status = "CREATED";
                createdAt = event.occurredAt();
                // Extraer datos del payload JSON
                extractOrderCreatedData(event.payload());
            }
            case "InventoryReserved" -> {
                status = "INVENTORY_RESERVED";
            }
            case "InventoryInsufficient" -> {
                status = "INVENTORY_FAILED";
            }
            case "PaymentCompleted" -> {
                status = "PAID";
            }
            case "PaymentFailed" -> {
                status = "PAYMENT_FAILED";
            }
            case "OrderShipped" -> {
                status = "SHIPPED";
                extractTrackingNumber(event.payload());
            }
            case "OrderDelivered" -> {
                status = "DELIVERED";
            }
            case "OrderCanceled" -> {
                status = "CANCELLED";
                extractCancelReason(event.payload());
            }
            default -> {
                // Evento desconocido — ignorar sin romper
            }
        }
    }

    private void extractOrderCreatedData(String payload) {
        // Parsing simple sin Jackson (teaching code)
        if (payload.contains("orderId")) {
            orderId = extractJsonField(payload, "orderId");
        }
        if (payload.contains("customerId")) {
            customerId = extractJsonField(payload, "customerId");
        }
        if (payload.contains("totalAmount")) {
            try {
                String val = extractJsonField(payload, "totalAmount");
                totalAmount = Double.parseDouble(val);
            } catch (NumberFormatException e) { /* ignore */ }
        }
    }

    private void extractTrackingNumber(String payload) {
        if (payload.contains("trackingNumber")) {
            trackingNumber = extractJsonField(payload, "trackingNumber");
        }
    }

    private void extractCancelReason(String payload) {
        if (payload.contains("reason")) {
            cancelReason = extractJsonField(payload, "reason");
        }
    }

    private String extractJsonField(String json, String field) {
        String pattern = "\"" + field + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int colonIdx = json.indexOf(":", idx);
        if (colonIdx < 0) return null;
        int start = json.indexOf("\"", colonIdx + 1);
        if (start < 0) {
            // Might be a number
            int numStart = colonIdx + 1;
            while (numStart < json.length() && json.charAt(numStart) == ' ') numStart++;
            int numEnd = numStart;
            while (numEnd < json.length() && (Character.isDigit(json.charAt(numEnd)) || json.charAt(numEnd) == '.')) numEnd++;
            return json.substring(numStart, numEnd);
        }
        int end = json.indexOf("\"", start + 1);
        return end > start ? json.substring(start + 1, end) : null;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public List<String> getItems() { return items; }
    public String getCancelReason() { return cancelReason; }
    public String getTrackingNumber() { return trackingNumber; }
    public int getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
}
