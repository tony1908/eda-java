package com.edacourse.api.shipping.application.service;

import com.edacourse.api.shipping.domain.model.Shipment;
import com.edacourse.api.shipping.domain.repository.ShipmentRepository;
import com.edacourse.api.shipping.domain.event.OrderShippedEvent;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;

public class ShippingService {
    private final EventBus eventBus;
    private final ShipmentRepository repository;

    public ShippingService(EventBus eventBus, ShipmentRepository repository) {
        this.eventBus = eventBus;
        this.repository = repository;
    }

    public void createShipment(String orderId) {
        Shipment shipment = new Shipment(orderId, "Dirección del cliente");
        shipment.ship();
        repository.save(shipment);

        System.out.println("Envío creado: " + shipment.getTrackingNumber() + " para orden " + orderId);
        eventBus.publish("shipping.shipped",
            new OrderShippedEvent(shipment.getId(), orderId, shipment.getTrackingNumber()),
            orderId);
    }
}
