package com.edacourse.api.order.infrastructure.persistence;

import com.edacourse.api.order.domain.model.Order;
import com.edacourse.api.order.domain.model.OrderStatus;
import com.edacourse.api.order.domain.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        orders.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    @Override
    public void updateStatus(String id, OrderStatus status, String reason) {
        Order order = orders.get(id);
        if (order != null) {
            switch (status) {
                case CANCELLED -> order.cancel(reason);
                case CONFIRMED -> order.confirm();
                case SHIPPED -> order.markShipped();
                case DELIVERED -> order.markDelivered();
                default -> {}
            }
        }
    }
}
