package com.edacourse.api;

import com.edacourse.api.shared.config.AppBinder;
import com.edacourse.api.shared.config.ObjectMapperProvider;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.shared.infrastructure.messaging.EventBusFactory;
import com.edacourse.api.shared.infrastructure.messaging.DeadLetterHandler;
import com.edacourse.api.shared.infrastructure.serialization.EventSerializer;
import com.edacourse.api.shared.infrastructure.serialization.JsonEventSerializer;

import com.edacourse.api.saga.application.service.CheckoutSagaOrchestrator;
import com.edacourse.api.saga.infrastructure.persistence.SagaStateRepository;
import com.edacourse.api.saga.interfaces.rest.SagaResource;

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

import com.edacourse.api.catalog.domain.repository.ProductRepository;
import com.edacourse.api.catalog.infrastructure.persistence.SqlServerProductRepository;
import com.edacourse.api.catalog.application.service.CatalogService;
import com.edacourse.api.catalog.interfaces.rest.CatalogResource;

import com.edacourse.api.search.application.service.SearchService;
import com.edacourse.api.search.infrastructure.subscriber.SearchSubscriber;

import com.edacourse.api.catalog.infrastructure.cdc.CdcStrategy;
import com.edacourse.api.catalog.infrastructure.cdc.NativeCdcStrategy;
import com.edacourse.api.catalog.infrastructure.cdc.PollingCdcStrategy;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import com.edacourse.api.catalog.infrastructure.cdc.TriggerOutboxStrategy;

import com.edacourse.api.search.domain.repository.ProductSearchRepository;
import com.edacourse.api.search.infrastructure.opensearch.OpenSearchProductSearchRepository;
import com.edacourse.api.search.infrastructure.opensearch.EmbeddingGenerator;
import com.edacourse.api.search.infrastructure.opensearch.TrigramEmbeddingGenerator;

import com.edacourse.api.search.interfaces.rest.SearchResource;

import com.edacourse.api.shared.infrastructure.sse.EventSseBroadcaster;
import com.edacourse.api.shared.infrastructure.interfaces.sse.SseEventBridge;
import com.edacourse.api.shared.infrastructure.interfaces.sse.EventSseResource;
import com.edacourse.api.shared.infrastructure.interfaces.rest.StaticFileResource;

import com.edacourse.api.backup.application.BackupService;
import com.edacourse.api.backup.domain.port.ProductExporter;
import com.edacourse.api.backup.domain.port.ProductSeeder;
import com.edacourse.api.backup.infrastructure.persistence.SqlServerProductExporter;
import com.edacourse.api.backup.infrastructure.persistence.SqlServerProductSeeder;
import com.edacourse.api.backup.interfaces.BackupResource;
import com.edacourse.api.backup.infrastructure.subscriber.BackupSubscriber;
import com.edacourse.api.backup.infrastructure.restic.ResticClient;

import com.edacourse.api.filestream.application.service.FileStreamService;
import com.edacourse.api.filestream.infrastructure.kafka.FileChunkConsumer;
import com.edacourse.api.filestream.infrastructure.kafka.FileChunkProducer;
import com.edacourse.api.filestream.interfaces.rest.FileStreamResource;

import com.edacourse.api.cqrs.infrastructure.persistence.OrderReadModelRepository;
import com.edacourse.api.cqrs.application.service.OrderQueryService;
import com.edacourse.api.cqrs.interfaces.rest.CqrsResource;
import com.edacourse.api.cqrs.infrastructure.projection.OrderProjection;

import com.edacourse.api.eventsourcing.infrastructure.persistence.SqlServerEventStore;
import com.edacourse.api.eventsourcing.application.service.EventSourcingService;
import com.edacourse.api.eventsourcing.infrastructure.subscriber.EventStoreSubscriber;
import com.edacourse.api.eventsourcing.interfaces.rest.EventSourcingResource;

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

        // SQL Server connection
        String sqlUrl = System.getenv().getOrDefault("SQLSERVER_URL", "jdbc:sqlserver://sqlserver:1433;databaseName=eventflow;encrypt=false");
        String sqlUser = System.getenv().getOrDefault("SQLSERVER_USER", "sa");
        String sqlPass = System.getenv().getOrDefault("SQLSERVER_PASSWORD", "EventFlow123!");

        // Catalog context
        ProductRepository productRepo = new SqlServerProductRepository(sqlUrl, sqlUser, sqlPass);
        CatalogService catalogService = new CatalogService(productRepo);

        // Search context
        String openSearchUrl = System.getenv().getOrDefault("OPENSEARCH_URL", "http://opensearch:9200");
        EmbeddingGenerator embeddingGenerator = new TrigramEmbeddingGenerator();
        ProductSearchRepository productSearchRepository = new OpenSearchProductSearchRepository(openSearchUrl, "products", embeddingGenerator);
        SearchService searchService = new SearchService(productSearchRepository);
        new SearchSubscriber(eventBus, searchService);

        // CDC
        CdcStrategy cdcStrategy = new PollingCdcStrategy(sqlUrl, sqlUser, sqlPass);
        cdcStrategy.start(eventBus, "products.changed");

        // SSE bridge
        new SseBridgeSubscriber(eventBus, serializer, sseResource);

        // SSE broadcaster
        EventSseBroadcaster sseBroadcaster = new EventSseBroadcaster();
        new SseEventBridge(eventBus, sseBroadcaster, serializer);
        
        String resticRepository = System.getenv().getOrDefault("RESTIC_REPOSITORY", "rest:http://restic-server:8000/");
        String resticPassword = System.getenv().getOrDefault("RESTIC_PASSWORD", "EventFlow123!");
        ResticClient resticClient = new ResticClient(resticRepository, resticPassword);

        // Backup context
        String dbUrl = System.getenv().getOrDefault("SQLSERVER_URL", "jdbc:sqlserver://sqlserver:1433;databaseName=eventflow;encrypt=false");
        String dbUser = System.getenv().getOrDefault("SQLSERVER_USER", "sa");
        String dbPassword = System.getenv().getOrDefault("SQLSERVER_PASSWORD", "EventFlow123!");

        ProductExporter productExporter = new SqlServerProductExporter(dbUrl, dbUser, dbPassword);
        ProductSeeder productSeeder = new SqlServerProductSeeder(dbUrl, dbUser, dbPassword);

        BackupService backupService = new BackupService(eventBus, resticClient, productExporter);
        
        BackupSubscriber backupSubscriber = new BackupSubscriber(eventBus, backupService);

        int chunkSize = Integer.parseInt(System.getenv().getOrDefault("CHUNK_SIZE_BYTES", "524288"));
        FileChunkProducer chunkProducer = new FileChunkProducer("file.chunks");

        // FileStream context
        FileStreamService fileStreamService = new FileStreamService(eventBus, chunkProducer, chunkSize);

        new FileChunkConsumer(eventBus, "file.chunks", "file-import-group");

        // CQRS read model
        OrderReadModelRepository orderQueryRepository = new OrderReadModelRepository();
        OrderQueryService queryService = new OrderQueryService(orderQueryRepository);

        // CQRS projection
        new OrderProjection(eventBus, orderQueryRepository);

        // Event Sourcing
        SqlServerEventStore eventStore = new SqlServerEventStore();
        EventSourcingService eventSourcingService = new EventSourcingService(eventStore);

        // Event Sourcing subscriber
        new EventStoreSubscriber(eventBus, new com.edacourse.api.shared.infrastructure.serialization.JsonEventSerializer(), eventStore);

        // Saga context
        SagaStateRepository sagaStateRepository = new SagaStateRepository();
        CheckoutSagaOrchestrator sagaOrchestrator = new CheckoutSagaOrchestrator(eventBus, sagaStateRepository);
        
        
        

        // DLQ handler (if broker supports it)
        if (eventBus instanceof DeadLetterHandler dlh) {
            dlh.onDeadLetter("orders.created", String.class, event ->
                System.err.println("[DLQ] Mensaje perdido en orders.created: " + event));
        }

        // Jersey HTTP server
        ResourceConfig config = new ResourceConfig()
                .register(new AppBinder(serializer, eventBus, sseResource, catalogService, searchService, sseBroadcaster, backupService, productSeeder, fileStreamService, queryService, eventSourcingService, sagaOrchestrator))
                .register(JacksonFeature.class)
                .register(MultiPartFeature.class)
                .register(ObjectMapperProvider.class)
                .register(OrderResource.class)
                .register(CatalogResource.class)
                .register(SearchResource.class)
                .register(StaticFileResource.class)
                .register(EventSseResource.class)
                .register(BackupResource.class)
                .register(FileStreamResource.class)
                .register(EventSourcingResource.class)
                .register(SagaResource.class)
                .register(CqrsResource.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

        System.out.println("=== EventFlow Platform iniciada ===");
        System.out.println("Broker: " + eventBus.getClass().getSimpleName());
        System.out.println("Contextos: Order, Inventory, Payment, Shipping, Notification, Catalog, Search");
        System.out.println("REST: " + BASE_URI + "api/orders");
        System.out.println("REST: " + BASE_URI + "api/products");
        System.out.println("SSE:  " + BASE_URI + "api/orders/events");
        System.out.println("REST: " + BASE_URI + "api/search");
        System.out.println("REST: " + BASE_URI + "api/backups");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Apagando EventFlow...");
            server.shutdownNow();
            eventBus.close();
        }));

        Thread.currentThread().join();
    }
}
