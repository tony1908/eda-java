package com.edacourse.api.search.application.dto;

import com.edacourse.api.search.domain.model.SearchResult;

public class SearchResultResponse {
    private final String productId;
    private final String name;
    private final String description;
    private final double price;
    private final String category;
    private final int stock;
    private final double score;

    private SearchResultResponse(SearchResult r) {
        this.productId = r.productId();
        this.name = r.name();
        this.description = r.description();
        this.price = r.price();
        this.category = r.category();
        this.stock = r.stock();
        this.score = r.score();
    }

    public static SearchResultResponse from(SearchResult r) {
        return new SearchResultResponse(r);
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public int getStock() { return stock; }
    public double getScore() { return score; }
}
