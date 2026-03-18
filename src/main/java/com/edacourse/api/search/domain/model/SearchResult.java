package com.edacourse.api.search.domain.model;

public record SearchResult(
    String productId,
    String name,
    String description,
    double price,
    String category,
    int stock,
    double score
) {}
