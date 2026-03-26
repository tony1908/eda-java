package com.edacourse.api.shared.config;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.shared.infrastructure.serialization.EventSerializer;
import com.edacourse.api.order.domain.repository.OrderRepository;
import com.edacourse.api.order.infrastructure.persistence.InMemoryOrderRepository;
import com.edacourse.api.order.application.service.OrderService;
import com.edacourse.api.order.interfaces.sse.OrderSseResource;
import com.edacourse.api.catalog.application.service.CatalogService;
import com.edacourse.api.search.application.service.SearchService;
import com.edacourse.api.backup.application.BackupService;
import com.edacourse.api.backup.domain.port.ProductSeeder;
import jakarta.inject.Singleton;
import com.edacourse.api.shared.infrastructure.sse.EventSseBroadcaster;
import com.edacourse.api.filestream.application.service.FileStreamService;
import com.edacourse.api.cqrs.application.service.OrderQueryService;
import com.edacourse.api.eventsourcing.application.service.EventSourcingService;

public class AppBinder extends AbstractBinder {
    private final EventSerializer serializer;
    private final EventBus eventBus;
    private final OrderSseResource sseResource;
    private final CatalogService catalogService;
    private final SearchService searchService;
    private final EventSseBroadcaster eventSseBroadcaster;
    private final BackupService backupService;
    private final ProductSeeder productSeeder;
    private final FileStreamService fileStreamService;
    private final OrderQueryService queryService;
    private final EventSourcingService eventSourcingService;

    public AppBinder(EventSerializer serializer,
        EventBus eventBus,
        OrderSseResource sseResource,
        CatalogService catalogService,
        SearchService searchService,
        EventSseBroadcaster eventSseBroadcaster,
        BackupService backupService,
        ProductSeeder productSeeder,
        FileStreamService fileStreamService,
        OrderQueryService queryService,
        EventSourcingService eventSourcingService
    ) {
        this.serializer = serializer;
        this.eventBus = eventBus;
        this.sseResource = sseResource;
        this.catalogService = catalogService;
        this.searchService = searchService;
        this.eventSseBroadcaster = eventSseBroadcaster;
        this.backupService = backupService;
        this.productSeeder = productSeeder;
        this.fileStreamService = fileStreamService;
        this.queryService = queryService;
        this.eventSourcingService = eventSourcingService;
    }

    @Override
    protected void configure() {
        bind(serializer).to(EventSerializer.class);
        bind(eventBus).to(EventBus.class);

        bind(InMemoryOrderRepository.class).to(OrderRepository.class).in(Singleton.class);
        bind(OrderService.class).to(OrderService.class).in(Singleton.class);
        bind(sseResource).to(OrderSseResource.class).in(Singleton.class);
        bind(catalogService).to(CatalogService.class);

        bind(searchService).to(SearchService.class);
        bind(eventSseBroadcaster).to(EventSseBroadcaster.class);

        bind(backupService).to(BackupService.class);
        bind(productSeeder).to(ProductSeeder.class);
        bind(fileStreamService).to(FileStreamService.class);
        bind(queryService).to(OrderQueryService.class);
        bind(eventSourcingService).to(EventSourcingService.class);
    }
}
