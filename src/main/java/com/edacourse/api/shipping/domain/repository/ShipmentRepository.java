package com.edacourse.api.shipping.domain.repository;

import com.edacourse.api.shipping.domain.model.Shipment;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository {
    void save(Shipment shipment);
    Optional<Shipment> findById(String id);
    Optional<Shipment> findByOrderId(String orderId);
    List<Shipment> findAll();
}
