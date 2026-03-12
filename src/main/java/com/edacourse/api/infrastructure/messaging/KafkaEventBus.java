package com.edacourse.api.infrastructure.messaging;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord; 
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class KafkaEventBus implements EventBus {

    private final KafkaProducer<String, String> producer;
    private final EventSerializer serializer;
    private final String bootstrapServers;
    private final List<KafkaConsumer<String, String>> consumers = new ArrayList<>();
    private volatile boolean running = true;

    public KafkaEventBus(EventSerializer serializer) {
        this.serializer = serializer;
        this.bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "30000");

        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        this.producer = new KafkaProducer<>(props);
        System.out.println("KafkaEventBus inicializado");
    }

    @Override
    public void publish(String topic, Object event) {
        publish(topic, event, null);
    }

    @Override
    public void publish(String topic, Object event, String partitionKey) {
        String json = serializer.serialize(event);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, partitionKey, json);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("Error al publicar el evento: " + exception.getMessage());
            } else {
                System.out.println("Evento publicado exitosamente");
            }
        });
        producer.flush();
    }

    @Override
    public <T> void subscribe(String topic, Class<T> eventType, EventHandler<T> handler) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "eventflow-" + topic);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(topic));
        consumers.add(consumer);

        Thread thread = new Thread(() -> {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                records.forEach(record -> {
                    T event = serializer.deserialize(record.value(), eventType);
                    handler.handle(event);
                });
            }
        }, "kafka-consumer-" + topic);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void close() {
        running = false;
        consumers.forEach(consumer -> consumer.close());
        producer.close();
    }
}
