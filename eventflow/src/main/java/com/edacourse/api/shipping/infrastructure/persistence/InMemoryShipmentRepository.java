package com.edacourse.api.shipping.infrastructure.persistence;

import com.edacourse.api.shipping.domain.model.Shipment;
import com.edacourse.api.shipping.domain.repository.ShipmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryShipmentRepository implements ShipmentRepository {
    private final Map<String, Shipment> shipments = new ConcurrentHashMap<>();

    @Override
    public void save(Shipment shipment) {
        shipments.put(shipment.getId(), shipment);
    }

    @Override
    public Optional<Shipment> findById(String id) {
        return Optional.ofNullable(shipments.get(id));
    }

    @Override
    public Optional<Shipment> findByOrderId(String orderId) {
        return shipments.values().stream()
            .filter(s -> s.getOrderId().equals(orderId))
            .findFirst();
    }

    @Override
    public List<Shipment> findAll() {
        return new ArrayList<>(shipments.values());
    }
}
