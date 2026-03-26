package com.edacourse.api.saga.interfaces.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;

import com.edacourse.api.saga.application.service.CheckoutSagaOrchestrator;
import com.edacourse.api.saga.domain.model.SagaState;

import java.util.List;

/**
 * Endpoints REST para monitorear Sagas de checkout.
 */
@Path("/api/saga")
@Produces(MediaType.APPLICATION_JSON)
public class SagaResource {

    @Inject
    private CheckoutSagaOrchestrator sagaOrchestrator;

    /**
     * POST /api/saga/checkout — Inicia un checkout como Saga
     */
    @POST
    @Path("/checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response startCheckout(CheckoutRequest request) {
        if (request == null || request.orderId == null || request.orderId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"orderId es requerido\"}")
                .build();
        }
        String sagaId = sagaOrchestrator.startCheckout(request.orderId);
        return Response.accepted()
            .entity("{\"sagaId\":\"" + sagaId + "\",\"orderId\":\"" + request.orderId + "\",\"status\":\"STARTED\",\"message\":\"Saga de checkout iniciada\"}")
            .build();
    }

    /**
     * GET /api/saga/{sagaId} — Estado de una saga
     */
    @GET
    @Path("/{sagaId}")
    public Response getSagaState(@PathParam("sagaId") String sagaId) {
        SagaState saga = sagaOrchestrator.getSagaState(sagaId);
        if (saga == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Saga no encontrada: " + sagaId + "\"}")
                .build();
        }
        return Response.ok(toJson(saga)).build();
    }

    /**
     * GET /api/saga — Lista sagas recientes
     */
    @GET
    public Response listSagas(@QueryParam("limit") @DefaultValue("20") int limit) {
        List<SagaState> sagas = sagaOrchestrator.getRecentSagas(limit);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < sagas.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(sagas.get(i)));
        }
        sb.append("]");
        return Response.ok(sb.toString()).build();
    }

    private String toJson(SagaState s) {
        return String.format(
            "{\"sagaId\":\"%s\",\"orderId\":\"%s\",\"status\":\"%s\",\"currentStep\":\"%s\"," +
            "\"completedSteps\":[%s],\"failureReason\":%s,\"startedAt\":\"%s\",\"completedAt\":%s}",
            s.getSagaId(), s.getOrderId(), s.getStatus(), s.getCurrentStep(),
            s.getCompletedSteps().stream().map(st -> "\"" + st + "\"").reduce((a, b) -> a + "," + b).orElse(""),
            s.getFailureReason() != null ? "\"" + s.getFailureReason() + "\"" : "null",
            s.getStartedAt(),
            s.getCompletedAt() != null ? "\"" + s.getCompletedAt() + "\"" : "null"
        );
    }

    public static class CheckoutRequest {
        public String orderId;
    }
}
