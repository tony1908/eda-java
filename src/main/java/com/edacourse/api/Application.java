package com.edacourse.api;

import com.edacourse.api.config.AppBinder;
import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.infrastructure.messaging.KafkaEventBus;
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

import java.net.URI;

public class Application {
    private static final String BASE_URI = "http://0.0.0.0:8080/";

    public static void main(String[] args) throws Exception {
        EventSerializer serializer = new JsonEventSerializer();
        EventBus eventBus = new KafkaEventBus(serializer);
        OrderSseResource sseResource = new OrderSseResource();
        InventoryService inventoryService = new InventoryService();

        ResourceConfig config = new ResourceConfig()
                .register(new AppBinder(serializer, eventBus, sseResource))
                .register(JacksonFeature.class)
                .register(ObjectMapperProvider.class)
                .register(OrderResource.class);

        new InventorySubcriber(eventBus, inventoryService);
        new SseBridgeSubscriber(eventBus, serializer, sseResource);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Apagando");
            server.shutdownNow();
            eventBus.close();
        }));

        Thread.currentThread().join();
    }
}
