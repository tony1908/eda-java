package com.edacourse.api.shipping.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Shipment {
    private final String id;
    private final String orderId;
    private String address;
    private ShipmentStatus status;
    private Instant shippedAt;
    private Instant deliveredAt;
    private final String trackingNumber;

    public Shipment(String orderId, String address) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.orderId = orderId;
        this.address = address;
        this.status = ShipmentStatus.PREPARING;
        this.trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public void ship() {
        this.status = ShipmentStatus.SHIPPED;
        this.shippedAt = Instant.now();
    }

    public void deliver() {
        this.status = ShipmentStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getAddress() { return address; }
    public ShipmentStatus getStatus() { return status; }
    public Instant getShippedAt() { return shippedAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public String getTrackingNumber() { return trackingNumber; }
}
