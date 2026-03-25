package com.edacourse.api.filestream.interfaces.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;

import com.edacourse.api.filestream.application.service.FileStreamService;

@Path("/api/files")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FileStreamResource {
    @Inject
    private FileStreamService fileStreamService;

    /**
     * POST /api/files/import?count=10000 — Genera CSV de prueba y lo envia por chunks via Kafka
     */
    @POST
    @Path("/import")
    public Response importCatalog(@QueryParam("count") @DefaultValue("1000") int count) {
        if (count < 1 || count > 50000) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"count debe estar entre 1 y 50000\"}")
                .build();
        }

        try {
            // Generar CSV de prueba
            String csvPath = fileStreamService.generateTestCsv(count, "/data/imports");

            // Dividir en chunks y enviar via Kafka
            String fileId = fileStreamService.sendFile(csvPath);

            return Response.accepted()
                .entity("{\"fileId\":\"" + fileId + "\",\"products\":" + count
                    + ",\"status\":\"streaming\",\"message\":\"Archivo dividido en chunks y enviado a Kafka. El consumidor reensamblara e importara.\"}")
                .build();
        } catch (Exception e) {
            return Response.serverError()
                .entity("{\"error\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }
}
