package com.edacourse.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import com.edacourse.api.domain.Order;

public class OrderResponse {
    private final String id;
    private final String product;
    private final double price;
    private final Instant createdAt;

    @JsonCreator
    public OrderResponse(
        @JsonProperty("id") String id,
        @JsonProperty("product") String product,
        @JsonProperty("price") double price,
        @JsonProperty("created_at") Instant createdAt
    ) {
        this.id = id;
        this.product = product;
        this.price = price;
        this.createdAt = createdAt;
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getProduct(),
            order.getPrice(),
            order.getCreatedAt()
        );
    }

    public String getId() { return id; }
    public String getProduct() { return product; }
    public double getPrice() { return price; }
    public Instant getCreatedAt() { return createdAt; }
}
