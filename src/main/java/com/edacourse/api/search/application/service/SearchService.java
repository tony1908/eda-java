package com.edacourse.api.search.application.service;

/**
 * Servicio de busqueda de productos.
 * Por ahora solo registra los cambios detectados.
 * Se integrara con OpenSearch para indexacion y busqueda.
 */
public class SearchService {

    public void indexProduct(String productId, String name, String description,
                             double price, String category, int stock, String operation) {
        System.out.println("[SEARCH] " + operation + " -> " + productId +
            " | " + name + " | $" + price + " | stock=" + stock + " | cat=" + category);
    }
}
