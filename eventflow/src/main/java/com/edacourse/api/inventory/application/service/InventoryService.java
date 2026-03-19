package com.edacourse.api.inventory.application.service;

import com.edacourse.api.inventory.domain.model.InventoryItem;
import com.edacourse.api.inventory.domain.model.Reservation;
import com.edacourse.api.inventory.domain.repository.InventoryRepository;
import com.edacourse.api.inventory.domain.event.InventoryReservedEvent;
import com.edacourse.api.inventory.domain.event.InventoryInsufficientEvent;
import com.edacourse.api.inventory.domain.event.StockLowEvent;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;

public class InventoryService {
    private static final int LOW_STOCK_THRESHOLD = 5;
    private final EventBus eventBus;
    private final InventoryRepository repository;

    public InventoryService(EventBus eventBus, InventoryRepository repository) {
        this.eventBus = eventBus;
        this.repository = repository;
    }

    public void reserveStock(String orderId, String productId, int quantity) {
        InventoryItem item = repository.findItemByProductId(productId)
            .orElseGet(() -> {
                InventoryItem newItem = new InventoryItem(productId, productId, 100);
                repository.saveItem(newItem);
                return newItem;
            });

        if (item.reserve(quantity)) {
            Reservation reservation = new Reservation(orderId, productId, quantity);
            repository.saveReservation(reservation);
            repository.saveItem(item);

            System.out.println("Stock reservado: " + productId + " x" + quantity + " para orden " + orderId);
            eventBus.publish("inventory.reserved",
                new InventoryReservedEvent(orderId, productId, quantity),
                orderId);

            if (item.getAvailableStock() <= LOW_STOCK_THRESHOLD) {
                eventBus.publish("inventory.stock-low",
                    new StockLowEvent(productId, item.getAvailableStock()),
                    productId);
            }
        } else {
            System.out.println("Stock insuficiente: " + productId + " (disponible: " + item.getAvailableStock() + ", solicitado: " + quantity + ")");
            eventBus.publish("inventory.insufficient",
                new InventoryInsufficientEvent(orderId, productId, quantity, item.getAvailableStock()),
                orderId);
        }
    }

    public void releaseStock(String orderId) {
        for (Reservation reservation : repository.findReservationsByOrderId(orderId)) {
            reservation.release();
            repository.findItemByProductId(reservation.getProductId())
                .ifPresent(item -> {
                    item.release(reservation.getQuantity());
                    repository.saveItem(item);
                });
        }
        System.out.println("Stock liberado para orden: " + orderId);
    }
}
