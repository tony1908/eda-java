package com.edacourse.solid;

import java.util.List;

public class Application {
    public static void main(String[] args) {
        try {
            Container container = new Container();
            container.register(EventBus.class, InMemoryEventBus.class);
            container.register(EventSerializer.class, JsonEventSerializer.class);
            container.register(NotificationService.class, ConsoleNotification.class);
            container.register(OrderService.class, OrderService.class);

            Thread.sleep(20000);

            EventBus eventBus = container.resolve(EventBus.class);
            eventBus.subscribe("orders", OrderEvent.class, event -> {
                System.out.println("Evento recibido: " + event);
            });

            OrderService orderService = container.resolve(OrderService.class);
            orderService.createOrder("Producto 1", 10.99);

            Thread.sleep(10000);

            eventBus.close();
        } catch(Exception e) {
            System.out.println("Fallo");
        }
        
    }
}
