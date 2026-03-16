package com.edacourse.api.payment.infrastructure.persistence;

import com.edacourse.api.payment.domain.model.Payment;
import com.edacourse.api.payment.domain.repository.PaymentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPaymentRepository implements PaymentRepository {
    private final Map<String, Payment> payments = new ConcurrentHashMap<>();

    @Override
    public void save(Payment payment) {
        payments.put(payment.getId(), payment);
    }

    @Override
    public Optional<Payment> findById(String id) {
        return Optional.ofNullable(payments.get(id));
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return payments.values().stream()
            .filter(p -> p.getOrderId().equals(orderId))
            .findFirst();
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(payments.values());
    }
}
