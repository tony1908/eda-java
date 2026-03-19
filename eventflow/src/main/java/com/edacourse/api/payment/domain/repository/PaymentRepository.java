package com.edacourse.api.payment.domain.repository;

import com.edacourse.api.payment.domain.model.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(String id);
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findAll();
}
