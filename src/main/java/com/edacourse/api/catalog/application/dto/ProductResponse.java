package com.edacourse.api.catalog.application.dto;

import com.edacourse.api.catalog.domain.model.Product;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductResponse {
    private final String id;
    private final String name;
    private final String description;
    private final double price;
    private final String category;
    private final int stock;
    @JsonProperty("created_at")
    private final String createdAt;
    @JsonProperty("updated_at")
    private final String updatedAt;

    private ProductResponse(Product p) {
        this.id = p.getId();
        this.name = p.getName();
        this.description = p.getDescription();
        this.price = p.getPrice();
        this.category = p.getCategory();
        this.stock = p.getStock();
        this.createdAt = p.getCreatedAt() != null ? p.getCreatedAt().toString() : null;
        this.updatedAt = p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null;
    }

    public static ProductResponse from(Product p) { return new ProductResponse(p); }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public int getStock() { return stock; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
