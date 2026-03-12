package com.edacourse.api.repository;

import com.edacourse.api.domain.Order;
import com.edacourse.api.domain.Order.Status;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
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
    public void updateStatus(String id, Status status, String reason) {
        Order order = orders.get(id);
        if (order != null) {
            order.cancel(reason);
        }
    }
}
