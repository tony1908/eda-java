package com.edacourse.api.inventory.domain.repository;

import com.edacourse.api.inventory.domain.model.InventoryItem;
import com.edacourse.api.inventory.domain.model.Reservation;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    void saveItem(InventoryItem item);
    Optional<InventoryItem> findItemByProductId(String productId);
    List<InventoryItem> findAllItems();
    void saveReservation(Reservation reservation);
    List<Reservation> findReservationsByOrderId(String orderId);
}
