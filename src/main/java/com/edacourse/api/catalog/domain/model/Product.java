package com.edacourse.api.catalog.domain.model;

import java.time.Instant;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private int stock;
    private Instant createdAt;
    private Instant updatedAt;

    public Product(String name, String description, double price, String category, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Product(String id, String name, String description, double price, String category, int stock, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public int getStock() { return stock; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; this.updatedAt = Instant.now(); }
    public void setDescription(String description) { this.description = description; this.updatedAt = Instant.now(); }
    public void setPrice(double price) { this.price = price; this.updatedAt = Instant.now(); }
    public void setCategory(String category) { this.category = category; this.updatedAt = Instant.now(); }
    public void setStock(int stock) { this.stock = stock; this.updatedAt = Instant.now(); }

    @Override
    public String toString() {
        return "Product{id='" + id + "', name='" + name + "', price=" + price + ", stock=" + stock + "}";
    }
}
