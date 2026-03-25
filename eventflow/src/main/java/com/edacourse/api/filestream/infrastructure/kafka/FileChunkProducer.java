package com.edacourse.api.filestream.infrastructure.kafka;

import com.edacourse.api.filestream.domain.model.FileChunk;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Properties;




public class FileChunkProducer {
    private final KafkaProducer<String, byte[]> producer;
    private final String topic;

    public FileChunkProducer(String topic) {
        this.topic = topic;

        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2 * 1024 * 1024); // 2MB max
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        this.producer = new KafkaProducer<>(props);
        System.out.println("[FILE-PRODUCER] Inicializado. Topic: " + topic);
    }

    public void sendChunk(FileChunk chunk) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, chunk.fileId(), chunk.data());

        // Metadata como Kafka headers
        record.headers()
            .add(new RecordHeader("fileId", chunk.fileId().getBytes(StandardCharsets.UTF_8)))
            .add(new RecordHeader("fileName", chunk.fileName().getBytes(StandardCharsets.UTF_8)))
            .add(new RecordHeader("partNumber", String.valueOf(chunk.partNumber()).getBytes(StandardCharsets.UTF_8)))
            .add(new RecordHeader("totalParts", String.valueOf(chunk.totalParts()).getBytes(StandardCharsets.UTF_8)))
            .add(new RecordHeader("checksum", chunk.checksum().getBytes(StandardCharsets.UTF_8)))
            .add(new RecordHeader("extension", chunk.extension().getBytes(StandardCharsets.UTF_8)));

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("[FILE-PRODUCER] Error enviando chunk " + chunk.partNumber() + "/" + chunk.totalParts() + ": " + exception.getMessage());
            } else {
                System.out.println("[FILE-PRODUCER] Chunk " + chunk.partNumber() + "/" + chunk.totalParts()
                    + " enviado (partition=" + metadata.partition() + ", offset=" + metadata.offset() + ")");
            }
        });

    }

    public void flush() {
        producer.flush();
    }

    public void close() {
        producer.close();
    }
}
