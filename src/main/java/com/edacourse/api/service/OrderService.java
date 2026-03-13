package com.edacourse.api.service;

import com.edacourse.api.repository.OrderRepository;
import com.edacourse.api.domain.Order;
import com.edacourse.api.infrastructure.messaging.RoutablePublisher;
import com.edacourse.api.domain.event.OrderCreatedEvent;
import com.edacourse.api.domain.event.OrderCanceledEvent;
import com.edacourse.api.dto.CreateOrderRequest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class OrderService {
    private final RoutablePublisher eventBus;
    private final OrderRepository repository;

    @Inject
    public OrderService(RoutablePublisher eventBus, OrderRepository repository) {
        this.eventBus = eventBus;
        this.repository = repository;
    }

    public Order createOrder(CreateOrderRequest dto) {
        System.out.println("Pedido creado");
        Order order = new Order(dto.getCustomerId(), dto.getProduct(), dto.getPrice(), dto.getQuantity());
        repository.save(order);
        eventBus.publish("orders.created", 
            "orders.created",
            new OrderCreatedEvent(
                order.getProduct(), order.getPrice()
            )
        );

        return order;
    }

    public void cancelOrder(String id, String reason) {
        repository.updateStatus(id, Order.Status.CANCELLED, reason);
        eventBus.publish("orders.canceled", "orders.canceled", new OrderCanceledEvent(id, reason));
    }
}
