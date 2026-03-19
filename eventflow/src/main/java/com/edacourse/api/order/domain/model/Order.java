package com.edacourse.api.order.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order {
    private final String id;
    private final String customerId;
    private final List<OrderItem> items;
    private final double totalAmount;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant cancelledAt;
    private String cancelReason;

    public Order(String customerId, List<OrderItem> items) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.totalAmount = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt = Instant.now();
    }

    public void markShipped() {
        this.status = OrderStatus.SHIPPED;
    }

    public void markDelivered() {
        this.status = OrderStatus.DELIVERED;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public double getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public String getCancelReason() { return cancelReason; }
}
