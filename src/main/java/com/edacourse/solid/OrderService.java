package com.edacourse.solid;

public class OrderService {
    private final NotificationService notificationService;
    private final EventBus eventBus;

    public OrderService(NotificationService notificationService, EventBus eventBus) {
        this.notificationService = notificationService;
        this.eventBus = eventBus;
    }

    public void createOrder(String product, double price) {
        System.out.println("Pedido creado");
        notificationService.notify("Nuevo pedido creado:" + product + " - " + price);
        eventBus.publish("orders", new OrderEvent(product, price));
    }
}
