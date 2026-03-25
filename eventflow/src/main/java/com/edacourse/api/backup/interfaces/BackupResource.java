package com.edacourse.api.backup.interfaces;

import com.edacourse.api.backup.application.BackupService;
import com.edacourse.api.backup.domain.port.ProductSeeder;
import com.edacourse.api.backup.domain.port.ProductSeeder.SeedResult;
import com.edacourse.api.backup.domain.dto.BackupRequestDTO;
import com.edacourse.api.backup.domain.dto.RestoreRequestDTO;
import com.edacourse.api.backup.domain.dto.BackupResponseDTO;
import com.edacourse.api.backup.domain.dto.RestoreResponseDTO;
import jakarta.inject.Inject;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Path("/api/backups")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BackupResource {

    @Inject
    private BackupService backupService;

    @Inject
    private ProductSeeder productSeeder;

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
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {

        if (fileInputStream == null || fileDetail == null || fileDetail.getFileName() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new BackupResponseDTO("", "No file provided")).build();
        }

        String fileName = fileDetail.getFileName();
        String uploadDir = backupService.getExportDir() + "/uploads";
        new File(uploadDir).mkdirs();
        String filePath = uploadDir + "/" + fileName;

        try {
            Files.copy(fileInputStream, new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return Response.serverError()
                .entity(new BackupResponseDTO("", "Failed to save file: " + e.getMessage())).build();
        }

        String backupId = backupService.requestFileBackup(fileName, filePath);
        return Response.ok(new BackupResponseDTO(backupId, "File backup requested for: " + fileName)).build();
    }

    @POST
    @Path("/seed")
    public Response seedData(@QueryParam("count") @DefaultValue("10000") int count) {
        SeedResult result = productSeeder.seed(count);
        return Response.ok(result).build();
    }
}
