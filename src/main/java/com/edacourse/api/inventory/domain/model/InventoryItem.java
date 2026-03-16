package com.edacourse.api.inventory.domain.model;

public class InventoryItem {
    private final String id;
    private final String productId;
    private int availableStock;
    private int reservedStock;

    public InventoryItem(String id, String productId, int availableStock) {
        this.id = id;
        this.productId = productId;
        this.availableStock = availableStock;
        this.reservedStock = 0;
    }

    public boolean reserve(int quantity) {
        if (availableStock >= quantity) {
            availableStock -= quantity;
            reservedStock += quantity;
            return true;
        }
        return false;
    }

    public void release(int quantity) {
        reservedStock -= quantity;
        availableStock += quantity;
    }

    public void confirmReservation(int quantity) {
        reservedStock -= quantity;
    }

    public String getId() { return id; }
    public String getProductId() { return productId; }
    public int getAvailableStock() { return availableStock; }
    public int getReservedStock() { return reservedStock; }
}
