package com.edacourse.api.order.domain.repository;

import com.edacourse.api.order.domain.model.Order;
import com.edacourse.api.order.domain.model.OrderStatus;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String id);
    List<Order> findAll();
    void updateStatus(String id, OrderStatus status, String reason);
}
