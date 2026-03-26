package com.edacourse.api.cqrs.domain.query;

/**
 * Query para obtener un pedido por ID desde el modelo de lectura.
 */
public record GetOrderQuery(String orderId) {}
