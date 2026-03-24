package com.edacourse.api.backup.interfaces;

import com.edacourse.api.backup.application.BackupService;
import com.edacourse.api.backup.application.DataSeeder;
import com.edacourse.api.backup.application.DataSeeder.SeedResult;
import com.edacourse.api.backup.domain.dto.BackupRequestDTO;
import com.edacourse.api.backup.domain.dto.RestoreRequestDTO;
import com.edacourse.api.backup.domain.dto.BackupResponseDTO;
import com.edacourse.api.backup.domain.dto.RestoreResponseDTO;
import jakarta.inject.Inject;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/backups")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BackupResource {

    @Inject
    private BackupService backupService;

    @Inject
    private DataSeeder dataSeeder;

    @POST
    @Path("/request")
    public Response requestBackup(BackupRequestDTO request) {
        String backupId = backupService.requestBackup(request.getDescription());
        return Response.ok(new BackupResponseDTO(backupId, "Backup requested")).build();
    }

    @POST
    @Path("/restore")
    public Response requestRestore(RestoreRequestDTO request) {
        String restoreId = backupService.requestRestore(request.getSnapshotId());
        return Response.ok(new RestoreResponseDTO(restoreId, "Restore requested")).build();
    }

    @GET
    @Path("/snapshots")
    public Response listSnapshots() {
        String snapshots = backupService.getSnapshots();
        return Response.ok(snapshots).build();
    }

    @GET
    @Path("/stats")
    public Response getStats() {
        String stats = backupService.getStats();
        return Response.ok(stats).build();
    }

    @POST
    @Path("/seed")
    public Response seedData(@QueryParam("count") @DefaultValue("10000") int count) {
        SeedResult result = dataSeeder.seed(count);
        return Response.ok(result).build();
    }
}
