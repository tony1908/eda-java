package com.edacourse.api;

import com.edacourse.api.shared.config.AppBinder;
import com.edacourse.api.shared.config.ObjectMapperProvider;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.shared.infrastructure.messaging.EventBusFactory;
import com.edacourse.api.shared.infrastructure.messaging.DeadLetterHandler;
import com.edacourse.api.shared.infrastructure.serialization.EventSerializer;
import com.edacourse.api.shared.infrastructure.serialization.JsonEventSerializer;

import com.edacourse.api.order.interfaces.rest.OrderResource;
import com.edacourse.api.order.interfaces.sse.OrderSseResource;
import com.edacourse.api.order.interfaces.sse.SseBridgeSubscriber;

import com.edacourse.api.inventory.domain.repository.InventoryRepository;
import com.edacourse.api.inventory.infrastructure.persistence.InMemoryInventoryRepository;
import com.edacourse.api.inventory.application.service.InventoryService;
import com.edacourse.api.inventory.infrastructure.subscriber.InventorySubscriber;

import com.edacourse.api.payment.domain.repository.PaymentRepository;
import com.edacourse.api.payment.infrastructure.persistence.InMemoryPaymentRepository;
import com.edacourse.api.payment.application.service.PaymentService;
import com.edacourse.api.payment.infrastructure.subscriber.PaymentSubscriber;

import com.edacourse.api.shipping.domain.repository.ShipmentRepository;
import com.edacourse.api.shipping.infrastructure.persistence.InMemoryShipmentRepository;
import com.edacourse.api.shipping.application.service.ShippingService;
import com.edacourse.api.shipping.infrastructure.subscriber.ShippingSubscriber;

import com.edacourse.api.notification.application.service.NotificationService;
import com.edacourse.api.notification.infrastructure.subscriber.NotificationSubscriber;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.grizzly.http.server.HttpServer;

import java.net.URI;

public class Application {
    private static final String BASE_URI = "http://0.0.0.0:8080/";

    public static void main(String[] args) throws Exception {
        // Shared infrastructure
        EventSerializer serializer = new JsonEventSerializer();
        EventBus eventBus = EventBusFactory.create(serializer);

        // Order context — interfaces
        OrderSseResource sseResource = new OrderSseResource();

        // Inventory context
        InventoryRepository inventoryRepo = new InMemoryInventoryRepository();
        InventoryService inventoryService = new InventoryService(eventBus, inventoryRepo);
        new InventorySubscriber(eventBus, inventoryService);

        // Payment context
        PaymentRepository paymentRepo = new InMemoryPaymentRepository();
        PaymentService paymentService = new PaymentService(eventBus, paymentRepo);
        new PaymentSubscriber(eventBus, paymentService);

        // Shipping context
        ShipmentRepository shipmentRepo = new InMemoryShipmentRepository();
        ShippingService shippingService = new ShippingService(eventBus, shipmentRepo);
        new ShippingSubscriber(eventBus, shippingService);

        // Notification context
        NotificationService notificationService = new NotificationService();
        new NotificationSubscriber(eventBus, notificationService);

        // SSE bridge
        new SseBridgeSubscriber(eventBus, serializer, sseResource);

        // DLQ handler (if broker supports it)
        if (eventBus instanceof DeadLetterHandler dlh) {
            dlh.onDeadLetter("orders.created", String.class, event ->
                System.err.println("[DLQ] Mensaje perdido en orders.created: " + event));
        }

        // Jersey HTTP server
        ResourceConfig config = new ResourceConfig()
                .register(new AppBinder(serializer, eventBus, sseResource))
                .register(JacksonFeature.class)
                .register(ObjectMapperProvider.class)
                .register(OrderResource.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

        System.out.println("=== EventFlow Platform iniciada ===");
        System.out.println("Broker: " + eventBus.getClass().getSimpleName());
        System.out.println("Contextos: Order, Inventory, Payment, Shipping, Notification");
        System.out.println("REST: " + BASE_URI + "api/orders");
        System.out.println("SSE:  " + BASE_URI + "api/orders/events");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Apagando EventFlow...");
            server.shutdownNow();
            eventBus.close();
        }));

        Thread.currentThread().join();
    }
}
