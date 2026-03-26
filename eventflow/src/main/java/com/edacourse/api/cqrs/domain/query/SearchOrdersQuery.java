package com.edacourse.api.cqrs.domain.query;

/**
 * Query para buscar pedidos con filtros.
 */
public record SearchOrdersQuery(
    String customerId,
    String status,
    int limit
) {
    public SearchOrdersQuery {
        if (limit <= 0) limit = 50;
    }
}
