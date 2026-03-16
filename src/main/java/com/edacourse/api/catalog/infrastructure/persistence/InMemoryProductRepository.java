package com.edacourse.api.catalog.infrastructure.persistence;

import com.edacourse.api.catalog.domain.model.Product;
import com.edacourse.api.catalog.domain.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProductRepository implements ProductRepository {
    private final ConcurrentHashMap<String, Product> products = new ConcurrentHashMap<>();

    @Override
    public Product save(Product product) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        product.setId(id);
        products.put(id, product);
        System.out.println("[CATALOG] Producto guardado: " + product);
        return product;
    }

    @Override
    public Product update(Product product) {
        if (!products.containsKey(product.getId())) {
            throw new RuntimeException("Producto no encontrado: " + product.getId());
        }
        products.put(product.getId(), product);
        System.out.println("[CATALOG] Producto actualizado: " + product);
        return product;
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }
}
