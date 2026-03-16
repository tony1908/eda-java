package com.edacourse.api.inventory.infrastructure.persistence;

import com.edacourse.api.inventory.domain.model.InventoryItem;
import com.edacourse.api.inventory.domain.model.Reservation;
import com.edacourse.api.inventory.domain.repository.InventoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryInventoryRepository implements InventoryRepository {
    private final Map<String, InventoryItem> items = new ConcurrentHashMap<>();
    private final List<Reservation> reservations = new CopyOnWriteArrayList<>();

    @Override
    public void saveItem(InventoryItem item) {
        items.put(item.getProductId(), item);
    }

    @Override
    public Optional<InventoryItem> findItemByProductId(String productId) {
        return Optional.ofNullable(items.get(productId));
    }

    @Override
    public List<InventoryItem> findAllItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public void saveReservation(Reservation reservation) {
        reservations.add(reservation);
    }

    @Override
    public List<Reservation> findReservationsByOrderId(String orderId) {
        return reservations.stream()
            .filter(r -> r.getOrderId().equals(orderId))
            .toList();
    }
}
