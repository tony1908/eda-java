package com.edacourse.api.domain;

import java.util.UUID;
import java.time.Instant;

public class Order {
    public enum Status { CREATED, CANCELLED}

    private final String id;
    private final String product;
    private final double price;
    private final int quantity;
    private Status status;
    private final Instant createdAt;
    private Instant cancelledAt;
    private String cancelReason;

    public Order(String product, double price, int quantity) {
        this.id = UUID.randomUUID().toString().substring(0,8);
        this.product = product;
        this.price = price;
        this.quantity = quantity;
        this.status = Status.CREATED;
        this.createdAt = Instant.now();
    }

    public void cancel(String reason) {
        this.status = Status.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt = Instant.now();
    }

    public String getId() { return id; }
    public String getProduct() { return product; }
    public double getPrice() { return price; }
    public Instant getCreatedAt() { return createdAt; }
}
