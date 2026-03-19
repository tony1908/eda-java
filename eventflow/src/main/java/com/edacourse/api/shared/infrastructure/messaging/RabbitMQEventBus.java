package com.edacourse.api.shared.infrastructure.messaging;

import com.edacourse.api.shared.infrastructure.serialization.EventSerializer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RabbitMQEventBus implements EventBus, DeadLetterHandler {
    private final Connection connection;
    private final Channel channel;
    private final EventSerializer serializer;

    public RabbitMQEventBus(EventSerializer serializer) {
        this.serializer = serializer;

        String host = System.getenv("RABBITMQ_HOST");
        int port = Integer.parseInt(System.getenv("RABBITMQ_PORT"));
        String username = System.getenv("RABBITMQ_USERNAME");
        String password = System.getenv("RABBITMQ_PASSWORD");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        Connection conn = null;
        Channel ch = null;

        for (int i = 0; i < 10; i++) {
            try {
                conn = factory.newConnection();
                ch = conn.createChannel();
                ch.confirmSelect();
                ch.basicQos(1);
                System.out.println("Conectado a RabbitMQ");
                break;
            } catch (Exception e) {
                System.out.println("Intentando conectar a RabbitMQ...");
                if (i == 9) {
                    throw new RuntimeException("No se pudo conectar a RabbitMQ", e);
                }
                try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }

        this.connection = conn;
        this.channel = ch;
    }

    @Override
    public void publish(String topic, Object event) {
        publish(topic, event, topic);
    }

    @Override
    public void publish(String topic, Object event, String partitionKey) {
        try {
            String routingKey = (partitionKey != null) ? partitionKey : topic;
            channel.exchangeDeclare(topic, "topic", true);
            String message = serializer.serialize(event);
            System.out.println("Publicando evento en " + topic + " con routing key: " + routingKey);
            channel.basicPublish(topic, routingKey, null, message.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Error al publicar evento", e);
        }
    }

    @Override
    public <T> void subscribe(String topic, Class<T> eventType, EventHandler<T> handler, String consumerGroup) {
        try {
            channel.exchangeDeclare(topic, "topic", true);

            String dlxExchange = topic + ".dlx";
            channel.exchangeDeclare(dlxExchange, "fanout", true);

            Map<String, Object> queueArgs = new HashMap<>();
            queueArgs.put("x-dead-letter-exchange", dlxExchange);

            String queue = channel.queueDeclare("", true, false, true, queueArgs).getQueue();
            channel.queueBind(queue, topic, "#");

            channel.basicConsume(queue, false, (consumerTag, delivery) -> {
                try {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    T event = serializer.deserialize(message, eventType);
                    handler.handle(event);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Exception e) {
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                }
            }, consumerTag -> {});
        } catch (Exception e) {
            throw new RuntimeException("Error al suscribirse al evento", e);
        }
    }

    @Override
    public <T> void onDeadLetter(String topic, Class<T> eventType, EventHandler<T> handler) {
        try {
            String dlxExchange = topic + ".dlx";
            String dlqName = topic + ".dlq";

            channel.exchangeDeclare(dlxExchange, "fanout", true);
            channel.queueDeclare(dlqName, true, false, false, null);
            channel.queueBind(dlqName, dlxExchange, "#");

            channel.basicConsume(dlqName, false, (consumerTag, delivery) -> {
                try {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    T event = serializer.deserialize(message, eventType);
                    handler.handle(event);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Exception e) {
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                }
            }, consumerTag -> {});
        } catch (Exception e) {
            throw new RuntimeException("Error al suscribirse al DLQ", e);
        }
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen()) channel.close();
            if (connection != null && connection.isOpen()) connection.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al cerrar conexión", e);
        }
    }
}
