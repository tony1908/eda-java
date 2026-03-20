package com.edacourse.pubsub;

import com.edacourse.pubsub.channel.infrastructure.messaging.KafkaEventBus;
import com.edacourse.pubsub.channel.infrastructure.persistance.SqlServerChannelRepository;
import com.edacourse.pubsub.channel.infrastructure.persistance.SqlSeverMessageRepository;
import com.edacourse.pubsub.channel.infrastructure.persistance.SqlServerSubscriptionRepository;
import com.edacourse.pubsub.channel.infrastructure.persistance.SqlServerDeliveryRepository;
import com.edacourse.pubsub.channel.infrastructure.messaging.EventBus;
import com.edacourse.pubsub.channel.infrastructure.security.HmacSigner;
import com.edacourse.pubsub.channel.infrastructure.security.HmacSignerService;
import com.edacourse.pubsub.channel.infrastructure.dispatcher.WebhookDispatcher;
import com.edacourse.pubsub.channel.repository.ChannelRepository;
import com.edacourse.pubsub.channel.repository.MessageRepository;
import com.edacourse.pubsub.channel.repository.SubscriptionRepository;
import com.edacourse.pubsub.channel.repository.DeliveryRespository;
import com.edacourse.pubsub.channel.service.ChannelService;
import com.edacourse.pubsub.channel.service.PublishService;
import com.edacourse.pubsub.channel.service.SubscriptionService;
import com.edacourse.pubsub.channel.config.AppBinder;
import com.edacourse.pubsub.channel.config.ObjectMapperProvider;

import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.edacourse.pubsub.channel.rest.ChannelResource;
import com.edacourse.pubsub.channel.rest.PublishResource;
import com.edacourse.pubsub.channel.rest.SubscriptionResource;
import com.edacourse.pubsub.channel.rest.WebhookResource;


public class Application {
    private static final String BASE_URL = "http://0.0.0.0:8080";

    public static void main(String[] args) throws InterruptedException {
        EventBus eventBus = new KafkaEventBus();
        ChannelRepository channelRepository = new SqlServerChannelRepository();
        MessageRepository messageRepository = new SqlSeverMessageRepository();
        SubscriptionRepository subscriptionRepository = new SqlServerSubscriptionRepository();
        DeliveryRespository deliveryRespository = new SqlServerDeliveryRepository();

        ChannelService channelService = new ChannelService(channelRepository);
        SubscriptionService subscriptionService = new SubscriptionService(subscriptionRepository, channelRepository);
        PublishService publishService = new PublishService(eventBus, messageRepository, channelRepository);

        HmacSigner hmacSigner = new HmacSignerService();

        WebhookDispatcher webhookDispatcher = new WebhookDispatcher(subscriptionRepository, deliveryRespository, hmacSigner);

        webhookDispatcher.start();

        ResourceConfig config = new ResourceConfig()
            .register(new AppBinder(channelService, subscriptionService, publishService, hmacSigner))
            .register(JacksonFeature.class)
            .register(ObjectMapperProvider.class)
            .register(PublishResource.class)
            .register(ChannelResource.class)
            .register(SubscriptionResource.class)
            .register(WebhookResource.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URL), config);
        System.out.println("Servidor corriendo en: " + BASE_URL);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Apagando PubSub Server...");
            server.shutdownNow();
            eventBus.close();
            webhookDispatcher.close();
        }));

        Thread.currentThread().join();
    }
}
