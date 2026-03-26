package com.edacourse.api.eventsourcing.interfaces.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;

import com.edacourse.api.eventsourcing.application.service.EventSourcingService;
import com.edacourse.api.eventsourcing.domain.model.StoredEvent;
import com.edacourse.api.eventsourcing.domain.model.OrderState;

import java.util.List;

/**
 * Endpoints REST para consultar el Event Store y reconstruir estado.
 */
@Path("/api/event-sourcing")
@Produces(MediaType.APPLICATION_JSON)
public class EventSourcingResource {

    @Inject
    private EventSourcingService eventSourcingService;

    /**
     * GET /api/event-sourcing/orders/{orderId} — Reconstruye el estado actual del pedido
     */
    @GET
    @Path("/orders/{orderId}")
    public Response reconstructOrder(@PathParam("orderId") String orderId) {
        OrderState state = eventSourcingService.reconstruct(orderId);
        if (state.getStatus() == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"No se encontraron eventos para el pedido: " + orderId + "\"}")
                .build();
        }

        String json = String.format(
            "{\"orderId\":\"%s\",\"customerId\":\"%s\",\"status\":\"%s\",\"totalAmount\":%.2f,\"trackingNumber\":%s,\"version\":%d,\"createdAt\":\"%s\",\"lastUpdatedAt\":\"%s\"}",
            state.getOrderId(), state.getCustomerId(), state.getStatus(), state.getTotalAmount(),
            state.getTrackingNumber() != null ? "\"" + state.getTrackingNumber() + "\"" : "null",
            state.getVersion(),
            state.getCreatedAt() != null ? state.getCreatedAt().toString() : "null",
            state.getLastUpdatedAt() != null ? state.getLastUpdatedAt().toString() : "null"
        );
        return Response.ok(json).build();
    }

    /**
     * GET /api/event-sourcing/orders/{orderId}/history — Historial completo de eventos
     */
    @GET
    @Path("/orders/{orderId}/history")
    public Response getOrderHistory(@PathParam("orderId") String orderId) {
        List<StoredEvent> events = eventSourcingService.getHistory(orderId);
        if (events.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"No se encontraron eventos para: " + orderId + "\"}")
                .build();
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < events.size(); i++) {
            StoredEvent e = events.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format(
                "{\"globalPosition\":%d,\"eventType\":\"%s\",\"payload\":%s,\"occurredAt\":\"%s\"}",
                e.globalPosition(), e.eventType(), e.payload(), e.occurredAt()
            ));
        }
        sb.append("]");
        return Response.ok(sb.toString()).build();
    }

    /**
     * GET /api/event-sourcing/orders — Lista todos los pedidos con eventos
     */
    @GET
    @Path("/orders")
    public Response listOrders() {
        List<String> orderIds = eventSourcingService.listOrderIds();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < orderIds.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(orderIds.get(i)).append("\"");
        }
        sb.append("]");
        return Response.ok(sb.toString()).build();
    }
}
