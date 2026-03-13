package com.edacourse.api;

import com.edacourse.api.config.AppBinder;
import com.edacourse.api.infrastructure.messaging.AdvancedEventBus;
import com.edacourse.api.infrastructure.messaging.RabbitMQEventBus;
import com.edacourse.api.infrastructure.messaging.EventSerializer;
import com.edacourse.api.infrastructure.messaging.JsonEventSerializer;
import com.edacourse.api.config.ObjectMapperProvider;
import com.edacourse.api.resource.OrderResource;
import com.edacourse.api.subscriber.InventorySubcriber;
import com.edacourse.api.subscriber.SseBridgeSubscriber;
import com.edacourse.api.resource.OrderSseResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import com.edacourse.api.service.InventoryService;
import com.edacourse.api.service.PaymentService;
import com.edacourse.api.service.NotificationService;
import com.edacourse.api.subscriber.PaymentSubscriber;
import com.edacourse.api.subscriber.NotificationSubscriber;
import com.edacourse.api.subscriber.DlqSubscriber;

import java.net.URI;

public class Application {
    private static final String BASE_URI = "http://0.0.0.0:8080/";

    public static void main(String[] args) throws Exception {
        EventSerializer serializer = new JsonEventSerializer();
        AdvancedEventBus eventBus = new RabbitMQEventBus(serializer);
        OrderSseResource sseResource = new OrderSseResource();
        InventoryService inventoryService = new InventoryService(eventBus);

        ResourceConfig config = new ResourceConfig()
                .register(new AppBinder(serializer, eventBus, sseResource))
                .register(JacksonFeature.class)
                .register(ObjectMapperProvider.class)
                .register(OrderResource.class);

        new InventorySubcriber(eventBus, inventoryService);
        new SseBridgeSubscriber(eventBus, serializer, sseResource);

        PaymentService paymentService = new PaymentService(eventBus);
        NotificationService notificationService = new NotificationService();

        new PaymentSubscriber(eventBus, paymentService);
        new NotificationSubscriber(eventBus, notificationService);

        new DlqSubscriber(eventBus);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Apagando");
            server.shutdownNow();
            eventBus.close();
        }));

        Thread.currentThread().join();
    }
}
