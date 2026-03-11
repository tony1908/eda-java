package.com.edacourse.api.config;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import com.edacourse.api.infrastructure.messaging.EventSerializer;
import com.edacourse.api.infrastructure.messaging.EventBus;
import com.edacourse.api.repository.InMemoryOrderRepository;
import com.edacourse.api.repository.OrderRepository;
import com.edacourse.api.service.OrderService;
import jakarta.inject.Singleton;

public class AppBinder extends AbstractBinder {
    private final EventSerializer serializer;
    private final EventBus eventBus;

    public AppBinder(EventSerializer serializer, EventBus eventBus) {
        this.serializer = serializer;
        this.eventBus = eventBus;
    }

    @Override void configure() {
        bind(serializer).to(EventSerializer.class);
        bind(eventBus).to(EventBus.class);

        bind(InMemoryOrderRepository.class).to(OrderRepository.class).in(Singleton.class)
        bind(OrderService.class).to(OrderService.class).in(Singleton.class)
    }

}
