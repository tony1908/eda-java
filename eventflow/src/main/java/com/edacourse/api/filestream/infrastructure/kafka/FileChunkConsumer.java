package com.edacourse.api.filestream.infrastructure.kafka;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.filestream.domain.event.FileUploadStartedEvent;
import com.edacourse.api.filestream.domain.model.FileChunk;
import com.edacourse.api.filestream.infrastructure.kafka.FileChunkProducer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.edacourse.api.filestream.domain.event.CatalogImportCompletedEvent;


public class FileChunkConsumer {
    private final EventBus eventBus;
    private final ConcurrentHashMap<String, TreeMap<Integer, byte[]>> fileBuffers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FileMetadata> fileMetadata = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    record FileMetadata(String fileName, int totalParts, long startTime) {}

    public FileChunkConsumer(EventBus eventBus, String topic, String groupId) {
        this.eventBus = eventBus;

        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 5 * 1024 * 1024); // 5MB fetch
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 2 * 1024 * 1024); // 2MB per partition

        Thread.startVirtualThread(() -> {
            try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(List.of(topic));
                System.out.println("[FILE-CONSUMER] Escuchando en topic: " + topic);

                while (running) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
                    for (ConsumerRecord<String, byte[]> record : records) {
                        processChunk(record);
                    }
                }
            }
        });
    }

    private void processChunk(ConsumerRecord<String, byte[]> record) {
        String fileId = getHeader(record, "fileId");
        String fileName = getHeader(record, "fileName");
        int partNumber = Integer.parseInt(getHeader(record, "partNumber"));
        int totalParts = Integer.parseInt(getHeader(record, "totalParts"));

        System.out.println("[FILE-CONSUMER] Recibido chunk " + partNumber + "/" + totalParts + " de " + fileName);

        // Almacenar metadata
        fileMetadata.putIfAbsent(fileId, new FileMetadata(fileName, totalParts, System.currentTimeMillis()));

        // Almacenar chunk ordenado por partNumber
        fileBuffers.computeIfAbsent(fileId, k -> new TreeMap<>()).put(partNumber, record.value());

        TreeMap<Integer, byte[]> chunks = fileBuffers.get(fileId);
        if (chunks.size() == totalParts) {
            System.out.println("[FILE-CONSUMER] Todos los chunks recibidos para " + fileName + ". Reensamblando...");
            reassembleAndImport(fileId);
        }
    }

    private void reassembleAndImport(String fileId) {
        FileMetadata meta = fileMetadata.get(fileId);
        TreeMap<Integer, byte[]> chunks = fileBuffers.remove(fileId);
        fileMetadata.remove(fileId);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (byte[] chunk : chunks.values()) {
                baos.write(chunk);
            }

            byte[] fileData = baos.toByteArray();
            System.out.println("[FILE-CONSUMER] Archivo reensamblado: " + fileData.length + " bytes");

            // Guardar archivo temporalmente
            Path restoreDir = Paths.get("/data/restores");
            if (!Files.exists(restoreDir)) {
                Files.createDirectories(restoreDir);
            }
            Path tempFile = Files.createTempFile(restoreDir, "import_", ".csv");
            Files.write(tempFile, fileData);
            System.out.println("[FILE-CONSUMER] Archivo guardado en: " + tempFile);
        } catch (Exception e) {
            System.err.println("[FILE-CONSUMER] Error reensamblando archivo: " + e.getMessage());
        }
    }

    private String getHeader(ConsumerRecord<String, byte[]> record, String key) {
        Header header = record.headers().lastHeader(key);
        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : "";
    }

    public void stop() {
        running = false;
    }

}
