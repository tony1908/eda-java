package com.edacourse.api.repository;

import com.edacourse.api.domain.Order;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String id);
    List<Order> findAll();
}
