package com.edacourse.api.service;

import com.edacourse.api.repository.OrderRepository;
import com.edacourse.api.model.Order;
import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.dto.CreateOrderRequest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class OrderService {
    private final EventBus eventBus;
    private final OrderRepository repository;

    @Inject
    public OrderService(EventBus eventBus, OrderRepository repository) {
        this.notificationService = notificationService;
        this.eventBus = eventBus;
        this.repository = repository;
    }

    public Order createOrder(CreateOrderRequest dto) {
        System.out.println("Pedido creado");
        Order order = new Order(dto.getProduct(), dto.getPrice(), dto.getQuantity())
        repository.save(order)
        eventBus.publish("orders", new OrderEvent(product, price));

        return order;
    }
}
