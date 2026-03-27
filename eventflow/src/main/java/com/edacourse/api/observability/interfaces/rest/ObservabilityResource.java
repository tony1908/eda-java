package com.edacourse.api.observability.interfaces.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;

import com.edacourse.api.observability.application.service.ObservabilityService;

/**
 * Endpoints REST para observabilidad del sistema de eventos.
 */
@Path("/api/observability")
@Produces(MediaType.APPLICATION_JSON)
public class ObservabilityResource {

    @Inject
    private ObservabilityService observabilityService;

    /**
     * GET /api/observability/dashboard — Dashboard de metricas
     */
    @GET
    @Path("/dashboard")
    public Response getDashboard() {
        return Response.ok(observabilityService.getDashboard()).build();
    }

    /**
     * GET /api/observability/traces?limit=50 — Trazas recientes
     */
    @GET
    @Path("/traces")
    public Response getTraces(@QueryParam("limit") @DefaultValue("50") int limit) {
        return Response.ok(observabilityService.getRecentTraces(limit)).build();
    }

    /**
     * GET /api/observability/traces/{correlationId} — Trazas de un flujo especifico
     */
    @GET
    @Path("/traces/{correlationId}")
    public Response getTraceByCorrelation(@PathParam("correlationId") String correlationId) {
        return Response.ok(observabilityService.getTracesByCorrelation(correlationId)).build();
    }

    /**
     * GET /api/observability/health — Health check del sistema
     */
    @GET
    @Path("/health")
    public Response healthCheck() {
        String dashboard = observabilityService.getDashboard();
        return Response.ok("{\"status\":\"UP\",\"metrics\":" + dashboard + "}").build();
    }
}
