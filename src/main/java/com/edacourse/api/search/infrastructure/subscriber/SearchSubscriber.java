package com.edacourse.api.search.infrastructure.subscriber;

import com.edacourse.api.search.application.service.SearchService;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;

/**
 * Suscriptor que escucha eventos ProductChanged desde CDC
 * y delega al SearchService para indexacion.
 */
public class SearchSubscriber {
    private final SearchService searchService;

    public SearchSubscriber(EventBus eventBus, SearchService searchService) {
        this.searchService = searchService;
        eventBus.subscribe("catalog.product-changed", ProductChangedData.class,
            this::onProductChanged, "search");
    }

    private void onProductChanged(ProductChangedData event) {
        searchService.indexProduct(
            event.productId, event.name, event.description,
            event.price, event.category, event.stock, event.operation
        );
    }

    // Local DTO to avoid cross-context imports
    public static class ProductChangedData {
        public String productId;
        public String name;
        public String description;
        public double price;
        public String category;
        public int stock;
        public String operation;
    }
}
