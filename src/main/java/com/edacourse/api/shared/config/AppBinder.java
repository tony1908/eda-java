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
import jakarta.inject.Singleton;

public class AppBinder extends AbstractBinder {
    private final EventSerializer serializer;
    private final EventBus eventBus;
    private final OrderSseResource sseResource;
    private final CatalogService catalogService;
    private final SearchService searchService;

    public AppBinder(EventSerializer serializer, EventBus eventBus, OrderSseResource sseResource, CatalogService catalogService, SearchService searchService) {
        this.serializer = serializer;
        this.eventBus = eventBus;
        this.sseResource = sseResource;
        this.catalogService = catalogService;
        this.searchService = searchService;
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
    }
}
