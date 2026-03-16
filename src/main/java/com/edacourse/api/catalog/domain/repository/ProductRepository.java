package com.edacourse.api.catalog.domain.repository;

import com.edacourse.api.catalog.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Product update(Product product);
    Optional<Product> findById(String id);
    List<Product> findAll();
}
