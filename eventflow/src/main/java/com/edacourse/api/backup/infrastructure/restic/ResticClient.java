package com.edacourse.api.backup.infrastructure.restic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ResticClient {
    private final String repository;
    private final String password;

    public ResticClient(String repository, String password) {
        this.repository = repository;
        this.password = password;

        initRepository();
    }

    private void initRepository() {
        try {
            ProcessBuilder pb = buildProcess("init");
            Process process = pb.start();
            String output = readOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.out.println("[RESTIC] Repositorio inicializado: " + repository);
            } else if (output.contains("already initialized")) {
                System.out.println("[RESTIC] Repositorio ya inicializado: " + repository);
            } else {
                System.out.println("[RESTIC] Error al inicializar repositorio: " + output);
            }
        } catch (Exception e) {
            System.err.println("[RESTIC] Error al inicializar: " + e.getMessage());
        }
    }

    public String backup(String directory) {
        try {
            System.out.println("[RESTIC] Iniciando backup de: " + directory);
            ProcessBuilder pb = buildProcess("backup","--json", directory);
            Process process = pb.start();
            String output = readOutput(process);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                String snapshotId = extractSnapshotId(output);
                System.out.println("[RESTIC] Backup completado. Snapshot: " + snapshotId);
                return snapshotId;
            } else {
                System.out.println("[RESTIC] Error en backup: " + output);
                return null;
            }
        } catch (Exception e) {
            System.err.println("[RESTIC] Error en backup: " + e.getMessage());
            return null;
        }
    }

    public boolean restore(String snapshotId, String targetDir) {
        try {
            System.out.println("[RESTIC] Restaurando snapshot: " + snapshotId);
            ProcessBuilder pb = buildProcess("restore", snapshotId, "--target", targetDir);
            Process process = pb.start();
            String output = readOutput(process);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("[RESTIC] Restauracion completada");
                return true;
            } else {
                System.out.println("[RESTIC] Error al restaurar: " + output);
                return false;
            }
        } catch (Exception e) {
            System.err.println("[RESTIC] Error al restaurar: " + e.getMessage());
            return false;
        }
    }

    public String listSnapshots() {
        try {
            ProcessBuilder pb = buildProcess("snapshots", "--json");
            Process process = pb.start();
            String output = readOutput(process);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("[RESTIC] Snapshots listados");
                return output;
            } else {
                System.out.println("[RESTIC] Error al listar snapshots: " + output);
                return "[]";
            }
        } catch (Exception e) {
            System.err.println("[RESTIC] Error al listar snapshots: " + e.getMessage());
            return  "[]";
        }
    }

    public String stats() {
        try {
            ProcessBuilder pb = buildProcess("stats", "--json");
            Process process = pb.start();
            String output = readOutput(process);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("[RESTIC] Estadisticas obtenidas");
                return output;
            } else {
                System.out.println("[RESTIC] Error al obtener estadisticas: " + output);
                return "{}";
            }
        } catch (Exception e) {
            System.err.println("[RESTIC] Error al obtener estadisticas: " + e.getMessage());
            return "{}";
        }
    }

    private ProcessBuilder buildProcess(String... args) {
        var command = new ArrayList<>(List.of("restic", "-r", repository));
        command.addAll(List.of(args));
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("RESTIC_PASSWORD", password);
        pb.redirectErrorStream(true);
        return pb;
    }

    private String readOutput(Process process) throws Exception {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private String extractSnapshotId(String jsonOutput) {
        for (String line : jsonOutput.split("\n")) {
            if (line.contains("snapshot_id")) {
                int start = line.indexOf("snapshot_id") + 14;
                int end = line.indexOf("\"", start);
                if (start > 14 && end > start) {
                    return line.substring(start, end);
                }
            }
        }

        String trimmed = jsonOutput.trim();
        if (trimmed.length() >= 8) {
            return trimmed.substring(trimmed.length() - 12).replaceAll("[^a-f0-9]", "");
        }

        return "unknown";
    }
}