package com.edacourse.api.backup.application;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.backup.infrastructure.restic.ResticClient;
import com.edacourse.api.backup.domain.event.*;

import java.io.*;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;

public class BackupService {
    private final EventBus eventBus;
    private final ResticClient resticClient;
    private final String exportDir;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;


    public BackupService(EventBus eventBus, ResticClient resticClient) {
        this.eventBus = eventBus;
        this.resticClient = resticClient;
        this.exportDir = System.getenv().getOrDefault("EXPORT_DIR", "/mnt/backups");
        this.dbUrl = System.getenv().getOrDefault("SQLSERVER_URL", "jdbc:sqlserver://sqlserver:1433;databaseName=eventflow;encrypt=false");
        this.dbUser = System.getenv().getOrDefault("SQLSERVER_USER", "sa");
        this.dbPassword = System.getenv().getOrDefault("SQLSERVER_PASSWORD", "EventFlow123!");

        new File(exportDir).mkdirs();
        System.out.println("[BACKUP] Servicio inicializado. Directorio de exportacion: " + exportDir);
    }

    public String requestBackup(String description) {
        String backupId = "bk_" + UUID.randomUUID().toString().substring(0, 8);

        eventBus.publish("backup.requested", new BackupRequestedEvent(backupId, description));

        return backupId;
    }

    public void executeBackup(String backupId, String description) {
        long startTime = System.currentTimeMillis();
        try {
            cleanExportDir();
            String exportFile = exportDir + "/products_" + backupId + ".json";
            int count = exportProductsToJson(exportFile);
            System.out.println("[BACKUP] " + count + " productos exportados a " + exportFile);

            String snapshotId = resticClient.backup(exportFile);

            if (snapshotId != null) {
                long duration = System.currentTimeMillis() - startTime;
                long fileSize = new File(exportFile).length();
                eventBus.publish("backup.completed",
                    new BackupCompletedEvent(backupId, snapshotId, fileSize, duration));
                System.out.println("[BACKUP] Completado en " + duration + "ms. Snapshot: " + snapshotId);
            } else {
                eventBus.publish("backup.failed",
                    new BackupFailedEvent(backupId, "Restic backup retorno null"));
            }

        } catch (Exception e) {
            eventBus.publish("backup.failed",
                new BackupFailedEvent(backupId, "Error al exportar: " + e.getMessage()));
        }
    }

    private int exportProductsToJson(String filePath) throws Exception {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, description, price, category, stock FROM products");
             PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            writer.println("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) writer.println(",");
                first = false;
                writer.printf("  {\"id\":\"%s\",\"name\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"category\":\"%s\",\"stock\":%d}",
                    rs.getString("id"),
                    escapeJson(rs.getString("name")),
                    escapeJson(rs.getString("description")),
                    rs.getDouble("price"),
                    escapeJson(rs.getString("category")),
                    rs.getInt("stock"));
                count++;
            }
            writer.println("\n]");
        }
        return count;
    }

    public String requestRestore(String snapshotId) {
        String restoreId = "rs_" + UUID.randomUUID().toString().substring(0, 8);

        eventBus.publish("restore.requested",
            new RestoreRequestedEvent(restoreId, snapshotId));

        return restoreId;
    }

    public void executeRestore(String restoreId, String snapshotId) {
        long startTime = System.currentTimeMillis();
        String restoreDir = exportDir + "/../restores/restore_" + restoreId;
        try {
            boolean success = resticClient.restore(snapshotId, restoreDir);
            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                File dir = new File(restoreDir);
                int fileCount = countFiles(dir);
                eventBus.publish("restore.completed",
                    new RestoreCompletedEvent(restoreId, snapshotId, fileCount, duration));
                System.out.println("[BACKUP] Restauracion completada: " + fileCount + " archivos en " + duration + "ms");
            } else {
                eventBus.publish("restore.failed",
                    new RestoreFailedEvent(restoreId, "Restic restore fallo"));
                System.out.println("[BACKUP] Restauracion fallida");
            }

        } catch (Exception e) {
            eventBus.publish("restore.failed",
                new RestoreFailedEvent(restoreId, "Error al restaurar: " + e.getMessage()));
        }
    }

    public String getSnapshots() {
        return resticClient.listSnapshots();
    }

    public String getStats() {
        return resticClient.stats();
    }

    private void cleanExportDir() {
        File dir = new File(exportDir);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) f.delete();
            }
        }
    }

    private int countFiles(File dir) {
        if (!dir.exists()) return 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        int count = 0;
        for (File f : files) {
            if (f.isFile()) count++;
            else count += countFiles(f);
        }
        return count;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
