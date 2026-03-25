package com.edacourse.api.filestream.application.service;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.filestream.domain.event.FileUploadStartedEvent;
import com.edacourse.api.filestream.domain.model.FileChunk;
import com.edacourse.api.filestream.infrastructure.kafka.FileChunkProducer;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

public class FileStreamService {
    private final EventBus eventBus;
    private final FileChunkProducer chunkProducer;
    private final int chunkSizeBytes;

    public FileStreamService(EventBus eventBus, FileChunkProducer chunkProducer, int chunkSizeBytes) {
        this.eventBus = eventBus;
        this.chunkProducer = chunkProducer;
        this.chunkSizeBytes = chunkSizeBytes;
        System.out.println("[FILE-STREAM] Servicio inicializado. Chunk size: " + chunkSizeBytes + " bytes");
    }

    public String sendFile(String filePath) throws IOException {
        String fileId = "file_" + UUID.randomUUID().toString().substring(0, 8);
        Path path = Path.of(filePath);
        String fileName = path.getFileName().toString();
        byte[] fileData = Files.readAllBytes(path);
        long totalBytes = fileData.length;

        int totalParts = (int) Math.ceil((double) totalBytes / chunkSizeBytes);

        System.out.println("[FILE-STREAM] Dividiendo " + fileName + " (" + totalBytes + " bytes) en " + totalParts + " chunks");

        // Publicar evento de inicio
        //eventBus.publish("file.upload.started",
        //    new FileUploadStartedEvent(fileId, fileName, totalParts, totalBytes, Instant.now()));

        for (int i = 0; i < totalParts; i++) {
            int start = i * chunkSizeBytes;
            int end = Math.min(start + chunkSizeBytes, fileData.length);
            byte[] chunkData = new byte[end - start];
            System.arraycopy(fileData, start, chunkData, 0, chunkData.length);
            String checksum = computeChecksum(chunkData);

            FileChunk chunk = new FileChunk(fileId, fileName, i + 1, totalParts, chunkData, checksum);
            System.out.println("[FILE-STREAM] Enviando chunk " + (i + 1) + "/" + totalParts + " de " + fileName);
            chunkProducer.sendChunk(chunk);
        }

        return fileId;
    }

    public String generateTestCsv(int productCount, String outputDir) throws IOException {
        new File(outputDir).mkdirs();
        String fileName = "catalog_import_" + productCount + ".csv";
        String filePath = outputDir + "/" + fileName;

        java.util.Random random = new java.util.Random();
        String[] names = {"Laptop", "Teclado", "Mouse", "Monitor", "Auriculares", "Webcam", "Tablet", "Impresora", "Router", "SSD"};
        String[] adjectives = {"Pro", "Ultra", "Max", "Elite", "Plus", "Gaming", "Office", "Premium", "Lite", "Smart"};
        String[] categories = {"Computadoras", "Perifericos", "Audio", "Redes", "Almacenamiento"};

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("name,description,price,category,stock");
            for (int i = 0; i < productCount; i++) {
                String name = names[random.nextInt(names.length)] + " " + adjectives[random.nextInt(adjectives.length)];
                String desc = "Producto de alta calidad para uso profesional";
                double price = Math.round((10 + random.nextDouble() * 1990) * 100.0) / 100.0;
                String category = categories[random.nextInt(categories.length)];
                int stock = 1 + random.nextInt(500);
                writer.printf("\"%s\",\"%s\",%.2f,%s,%d%n", name, desc, price, category, stock);
            }
        }

        long fileSize = new File(filePath).length();
        System.out.println("[FILE-STREAM] CSV generado: " + filePath + " (" + fileSize + " bytes, " + productCount + " productos)");
        return filePath;
    }

    private String computeChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            return "no-checksum";
        }
    }
}
