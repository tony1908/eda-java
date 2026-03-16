package com.edacourse.api.payment.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Payment {
    private final String id;
    private final String orderId;
    private final double amount;
    private PaymentStatus status;
    private Instant processedAt;

    public Payment(String orderId, double amount) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.orderId = orderId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    public void complete() {
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = Instant.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
        this.processedAt = Instant.now();
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
        this.processedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public Instant getProcessedAt() { return processedAt; }
}
