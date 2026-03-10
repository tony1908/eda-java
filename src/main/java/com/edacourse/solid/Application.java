package com.edacourse.solid;

import java.util.List;

public class Application {
    public static void main(String[] args) {
        Container container = new Container();
        container.register(EventBus.class, RabbitMQEventBus.class);
        container.register(EventSerializer.class, JsonEventSerializer.class);
        container.register(NotificationService.class, ConsoleNotification.class);
        container.register(OrderService.class, OrderService.class);

        EventBus eventBus = container.resolve(EventBus.class);
        eventBus.subscribe("orders", OrderEvent.class, event -> {
            System.out.println("Evento recibido: " + event);
        });

        OrderService orderService = container.resolve(OrderService.class);
        orderService.createOrder("Producto 1", 10.99);
        
    }
}
