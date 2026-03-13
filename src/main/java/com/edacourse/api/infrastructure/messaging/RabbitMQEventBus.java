package com.edacourse.api.infrastructure.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class RabbitMQEventBus implements EventBus {
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
                System.out.println("Conectado a RabbitMQ");
                break;
            } catch (Exception e) {
                System.out.println("Intentando conectar a RabbitMQ...");
                if (i == 9) {
                    throw new RuntimeException("No se pudo conectar a RabbitMQ", e);
                }
            }
        }

        this.connection = conn;
        this.channel = ch; 
    }

    @Override
    public void publish(String topic, Object event) {
        publish(topic, event, "");
    }

    @Override
    public void publish(String topic, Object event, String key) {
        try {
            channel.exchangeDeclare(topic, "fanout", true);
            String message = serializer.serialize(event);
            channel.basicPublish(topic, "", null, message.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Error al publicar el evento", e);
        }
    }
 
    @Override
    public <T> void subscribe(String topic, Class<T> eventType, EventHandler<T> handler, String consumerGroup) {
        try {
            channel.exchangeDeclare(topic, "fanout", true);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, topic, "");

            channel.basicConsume(queue, true, (consumerTag, delivery) -> {
                String json = new String(delivery.getBody(), StandardCharsets.UTF_8);
                T event = serializer.deserialize(json, eventType);
                handler.handle(event);
            }, consumerTag -> {});
            
        } catch (Exception e) {
            throw new RuntimeException("Error al suscribirse al evento", e);
        }
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen()) channel.close();
            if (connection != null && connection.isOpen()) connection.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al cerrar la conexión", e);
        }
    }
    
    
}
