package com.edacourse.pubsub.channel.infrastructure.messaging;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;


public class KafkaEventBus implements EventBus {
    private final KafkaProducer<String, String> producer;

    public KafkaEventBus() {
        Properties props = new Properties();
        props.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        this.producer = new KafkaProducer<>(props);
    }

    @Override
    public void publish(String channelName, String messageId, String payload) {
        String topic = "pubsub." + channelName;
        producer.send(new ProducerRecord<>(topic, messageId, payload), (metadata, exception) -> {
            if (exception != null) {
                System.err.println("Error publishing to topic " + topic + ": " + exception.getMessage());
            } else {
                System.out.println("Message published to topic " + topic + " with key " + messageId);
            }
        });
    }

    @Override
    public void close() {
        producer.close();
    }
}
