package com.edacourse.api.catalog.application.service;

import com.edacourse.api.catalog.application.dto.CreateProductRequest;
import com.edacourse.api.catalog.application.dto.UpdateProductRequest;
import com.edacourse.api.catalog.domain.model.Product;
import com.edacourse.api.catalog.domain.repository.ProductRepository;

import java.util.List;

public class CatalogService {
    private final ProductRepository repository;

    public CatalogService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product createProduct(CreateProductRequest dto) {
        Product product = new Product(
            dto.getName(), dto.getDescription(), dto.getPrice(),
            dto.getCategory(), dto.getStock()
        );
        return repository.save(product);
    }

    public Product updateProduct(String id, UpdateProductRequest dto) {
        Product product = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() > 0) product.setPrice(dto.getPrice());
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());
        if (dto.getStock() >= 0) product.setStock(dto.getStock());
        return repository.update(product);
    }

    public List<Product> listProducts() {
        return repository.findAll();
    }

    public Product getProduct(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
    }
}
