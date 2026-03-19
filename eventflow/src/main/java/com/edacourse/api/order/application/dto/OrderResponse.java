package com.edacourse.api.order.application.dto;

import com.edacourse.api.order.domain.model.Order;
import com.edacourse.api.order.domain.model.OrderItem;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class OrderResponse {
    private final String id;
    private final String customerId;
    private final List<ItemResponse> items;
    private final double totalAmount;
    private final String status;
    private final Instant createdAt;

    public OrderResponse(String id, String customerId, List<ItemResponse> items,
                         double totalAmount, String status, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static OrderResponse from(Order order) {
        List<ItemResponse> itemResponses = order.getItems().stream()
            .map(i -> new ItemResponse(i.getProductId(), i.getProductName(), i.getPrice(), i.getQuantity()))
            .toList();
        return new OrderResponse(
            order.getId(), order.getCustomerId(), itemResponses,
            order.getTotalAmount(), order.getStatus().name(), order.getCreatedAt()
        );
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public List<ItemResponse> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    @JsonProperty("created_at")
    public Instant getCreatedAt() { return createdAt; }

    public record ItemResponse(String productId, String productName, double price, int quantity) {}
}
