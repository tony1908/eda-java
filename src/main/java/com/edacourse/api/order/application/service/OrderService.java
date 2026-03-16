package com.edacourse.api.order.application.service;

import com.edacourse.api.order.domain.model.Order;
import com.edacourse.api.order.domain.model.OrderItem;
import com.edacourse.api.order.domain.repository.OrderRepository;
import com.edacourse.api.order.domain.event.OrderCreatedEvent;
import com.edacourse.api.order.domain.event.OrderCanceledEvent;
import com.edacourse.api.order.application.dto.CreateOrderRequest;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class OrderService {
    private final EventBus eventBus;
    private final OrderRepository repository;

    @Inject
    public OrderService(EventBus eventBus, OrderRepository repository) {
        this.eventBus = eventBus;
        this.repository = repository;
    }

    public Order createOrder(CreateOrderRequest dto) {
        List<OrderItem> items = dto.getItems().stream()
            .map(i -> new OrderItem(i.getProductId(), i.getProductName(), i.getPrice(), i.getQuantity()))
            .toList();

        Order order = new Order(dto.getCustomerId(), items);
        repository.save(order);

        List<OrderCreatedEvent.OrderItemData> itemData = items.stream()
            .map(i -> new OrderCreatedEvent.OrderItemData(
                i.getProductId(), i.getProductName(), i.getPrice(), i.getQuantity()))
            .toList();

        eventBus.publish("orders.created",
            new OrderCreatedEvent(order.getId(), order.getCustomerId(), itemData, order.getTotalAmount()),
            order.getId());

        System.out.println("Pedido creado: " + order.getId());
        return order;
    }

    public void cancelOrder(String id, String reason) {
        repository.updateStatus(id, com.edacourse.api.order.domain.model.OrderStatus.CANCELLED, reason);
        eventBus.publish("orders.canceled",
            new OrderCanceledEvent(id, reason),
            id);
    }
}
