package com.edacourse.api.inventory.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Reservation {
    private final String id;
    private final String orderId;
    private final String productId;
    private final int quantity;
    private ReservationStatus status;
    private final Instant createdAt;

    public Reservation(String orderId, String productId, int quantity) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = ReservationStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void confirm() { this.status = ReservationStatus.CONFIRMED; }
    public void release() { this.status = ReservationStatus.RELEASED; }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
