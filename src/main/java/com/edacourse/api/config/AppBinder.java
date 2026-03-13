package com.edacourse.api.config;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import com.edacourse.api.infrastructure.messaging.EventSerializer;
import com.edacourse.api.infrastructure.messaging.RoutablePublisher;
import com.edacourse.api.repository.InMemoryOrderRepository;
import com.edacourse.api.repository.OrderRepository;
import com.edacourse.api.service.OrderService;
import com.edacourse.api.resource.OrderSseResource;
import jakarta.inject.Singleton;

public class AppBinder extends AbstractBinder {
    private final EventSerializer serializer;
    private final RoutablePublisher eventBus;
    private final OrderSseResource sseResource;

    public AppBinder(EventSerializer serializer, RoutablePublisher eventBus, OrderSseResource sseResource) {
        this.serializer = serializer;
        this.eventBus = eventBus;
        this.sseResource = sseResource;
    }

    @Override
    protected void configure() {
        bind(serializer).to(EventSerializer.class);
        bind(eventBus).to(RoutablePublisher.class);

        bind(InMemoryOrderRepository.class).to(OrderRepository.class).in(Singleton.class);
        bind(OrderService.class).to(OrderService.class).in(Singleton.class);
        bind(sseResource).to(OrderSseResource.class).in(Singleton.class);
    }

}
