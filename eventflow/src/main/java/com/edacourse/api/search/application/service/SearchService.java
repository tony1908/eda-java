package com.edacourse.api.search.application.service;

import com.edacourse.api.search.domain.model.SearchableProduct;
import com.edacourse.api.search.domain.model.SearchResult;
import com.edacourse.api.search.domain.repository.ProductSearchRepository;

import java.time.Instant;
import java.util.List;

public class SearchService {

    private final ProductSearchRepository searchRepository;

    public SearchService(ProductSearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public void indexProduct(String productId, String name, String description,
                             double price, String category, int stock, String operation) {
        System.out.println("[SEARCH] " + operation + " -> " + productId +
            " | " + name + " | $" + price + " | stock=" + stock + " | cat=" + category);

        SearchableProduct product = new SearchableProduct(
            productId, name, description, price, category, stock, Instant.now()
        );
        searchRepository.index(product);
    }

    public List<SearchResult> searchFullText(String query) {
        return searchRepository.searchByText(query, 10);
    }

    public List<SearchResult> searchSemantic(String query) {
        return searchRepository.searchBySemantic(query, 10);
    }

    public List<SearchResult> searchHybrid(String query) {
        return searchRepository.searchHybrid(query, 10);
    }
}
