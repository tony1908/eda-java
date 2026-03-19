package com.edacourse.api.catalog.infrastructure.persistence;

import com.edacourse.api.catalog.domain.model.Product;
import com.edacourse.api.catalog.domain.repository.ProductRepository;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SqlServerProductRepository implements ProductRepository {
    private final String jdbcUrl;
    private final String user;
    private final String password;

    public SqlServerProductRepository(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    @Override
    public Product save(Product product) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String sql = "INSERT INTO products (id, name, description, price, category, stock) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setString(5, product.getCategory());
            ps.setInt(6, product.getStock());
            ps.executeUpdate();
            product.setId(id);
            System.out.println("[CATALOG] Producto guardado: " + product);
            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando producto: " + e.getMessage(), e);
        }
    }

    @Override
    public Product update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, category = ?, stock = ?, updated_at = GETDATE() WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getCategory());
            ps.setInt(5, product.getStock());
            ps.setString(6, product.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Producto no encontrado: " + product.getId());
            }
            System.out.println("[CATALOG] Producto actualizado: " + product);
            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando producto: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Product> findById(String id) {
        String sql = "SELECT id, name, description, price, category, stock, created_at, updated_at FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando producto: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT id, name, description, price, category, stock, created_at, updated_at FROM products";
        List<Product> products = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando productos: " + e.getMessage(), e);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("price"),
            rs.getString("category"),
            rs.getInt("stock"),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at").toInstant()
        );
    }
}
