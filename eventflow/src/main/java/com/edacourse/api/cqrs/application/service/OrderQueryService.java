package com.edacourse.api.cqrs.application.service;

import com.edacourse.api.cqrs.domain.model.OrderReadModel;
import com.edacourse.api.cqrs.domain.query.GetOrderQuery;
import com.edacourse.api.cqrs.domain.query.SearchOrdersQuery;
import com.edacourse.api.cqrs.infrastructure.persistence.OrderReadModelRepository;

import java.util.List;

/**
 * Servicio de consultas — lado READ de CQRS.
 * Lee SOLO del modelo de lectura, nunca del modelo de escritura.
 */
public class OrderQueryService {

    private final OrderReadModelRepository readRepo;

    public OrderQueryService(OrderReadModelRepository readRepo) {
        this.readRepo = readRepo;
        System.out.println("[CQRS-QUERY] Servicio de consultas inicializado");
    }

    public OrderReadModel getOrder(GetOrderQuery query) {
        return readRepo.findById(query.orderId());
    }

    public List<OrderReadModel> searchOrders(SearchOrdersQuery query) {
        if (query.customerId() != null && !query.customerId().isBlank()) {
            return readRepo.findByCustomer(query.customerId(), query.limit());
        }
        if (query.status() != null && !query.status().isBlank()) {
            return readRepo.findByStatus(query.status(), query.limit());
        }
        return readRepo.findRecent(query.limit());
    }

    public String getStats() {
        return readRepo.getStats();
    }
}
