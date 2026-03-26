package com.edacourse.api.cqrs.interfaces.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;

import com.edacourse.api.cqrs.application.service.OrderQueryService;
import com.edacourse.api.cqrs.domain.model.OrderReadModel;
import com.edacourse.api.cqrs.domain.query.GetOrderQuery;
import com.edacourse.api.cqrs.domain.query.SearchOrdersQuery;

import java.util.List;

/**
 * Endpoints REST para el lado de LECTURA de CQRS.
 * El lado de escritura ya existe en OrderResource (POST /api/orders).
 */
@Path("/api/cqrs")
@Produces(MediaType.APPLICATION_JSON)
public class CqrsResource {

    @Inject
    private OrderQueryService queryService;

    /**
     * GET /api/cqrs/orders/{orderId} — Consulta rapida desde el modelo de lectura
     */
    @GET
    @Path("/orders/{orderId}")
    public Response getOrder(@PathParam("orderId") String orderId) {
        OrderReadModel order = queryService.getOrder(new GetOrderQuery(orderId));
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Pedido no encontrado en modelo de lectura: " + orderId + "\"}")
                .build();
        }
        return Response.ok(toJson(order)).build();
    }

    /**
     * GET /api/cqrs/orders?customerId=X&status=Y&limit=N — Buscar pedidos
     */
    @GET
    @Path("/orders")
    public Response searchOrders(
            @QueryParam("customerId") String customerId,
            @QueryParam("status") String status,
            @QueryParam("limit") @DefaultValue("50") int limit) {
        List<OrderReadModel> orders = queryService.searchOrders(
            new SearchOrdersQuery(customerId, status, limit));

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(orders.get(i)));
        }
        sb.append("]");
        return Response.ok(sb.toString()).build();
    }

    /**
     * GET /api/cqrs/stats — Estadisticas del modelo de lectura
     */
    @GET
    @Path("/stats")
    public Response getStats() {
        return Response.ok(queryService.getStats()).build();
    }

    private String toJson(OrderReadModel o) {
        return String.format(
            "{\"orderId\":\"%s\",\"customerId\":\"%s\",\"status\":\"%s\",\"statusLabel\":\"%s\"," +
            "\"totalAmount\":%.2f,\"trackingNumber\":%s,\"itemCount\":%d,\"itemsSummary\":\"%s\"," +
            "\"createdAt\":\"%s\",\"lastUpdatedAt\":\"%s\",\"eventCount\":%d}",
            o.orderId(), o.customerId(), o.status(), o.statusLabel(),
            o.totalAmount(),
            o.trackingNumber() != null ? "\"" + o.trackingNumber() + "\"" : "null",
            o.itemCount(), o.itemsSummary(),
            o.createdAt(), o.lastUpdatedAt(), o.eventCount()
        );
    }
}
