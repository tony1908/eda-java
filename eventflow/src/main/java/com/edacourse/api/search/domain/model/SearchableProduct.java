package com.edacourse.api.search.domain.model;

import java.time.Instant;

public record SearchableProduct(
    String productId,
    String name,
    String description,
    double price,
    String category,
    int stock,
    Instant indexedAt
) {
    public String textForEmbedding() {
        return name + " " + description + " " + category;
    }
}
