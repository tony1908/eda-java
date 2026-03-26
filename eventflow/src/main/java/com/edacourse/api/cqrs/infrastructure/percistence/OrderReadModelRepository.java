package com.edacourse.api.cqrs.infrastructure.persistence;

import com.edacourse.api.cqrs.domain.model.OrderReadModel;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio para el modelo de lectura de pedidos.
 * Se actualiza SOLO via proyecciones de eventos.
 * Las consultas leen de aqui — nunca del modelo de escritura.
 */
public class OrderReadModelRepository {

    private final String dbUrl, dbUser, dbPassword;

    public OrderReadModelRepository() {
        this.dbUrl = System.getenv("SQLSERVER_URL");
        this.dbUser = System.getenv("SQLSERVER_USER");
        this.dbPassword = System.getenv("SQLSERVER_PASSWORD");
        System.out.println("[CQRS-READ] Repositorio de lectura inicializado");
    }

    /**
     * Inserta un nuevo pedido en el modelo de lectura.
     */
    public void insert(String orderId, String customerId, double totalAmount, int itemCount, String itemsSummary) {
        String sql = "INSERT INTO order_read_model (order_id, customer_id, status, status_label, total_amount, item_count, items_summary) " +
                     "VALUES (?, ?, 'CREATED', 'Pedido Creado', ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ps.setString(2, customerId);
            ps.setDouble(3, totalAmount);
            ps.setInt(4, itemCount);
            ps.setString(5, itemsSummary);
            ps.executeUpdate();
            System.out.println("[CQRS-READ] Pedido insertado: " + orderId);
        } catch (SQLException e) {
            System.err.println("[CQRS-READ] Error insertando: " + e.getMessage());
        }
    }

    /**
     * Actualiza el estado de un pedido en el modelo de lectura.
     */
    public void updateStatus(String orderId, String status, String statusLabel) {
        String sql = "UPDATE order_read_model SET status = ?, status_label = ?, last_updated_at = GETDATE(), event_count = event_count + 1 WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, statusLabel);
            ps.setString(3, orderId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                System.out.println("[CQRS-READ] Estado actualizado: " + orderId + " -> " + status);
            }
        } catch (SQLException e) {
            System.err.println("[CQRS-READ] Error actualizando: " + e.getMessage());
        }
    }

    /**
     * Actualiza el tracking number.
     */
    public void updateTracking(String orderId, String trackingNumber) {
        String sql = "UPDATE order_read_model SET tracking_number = ?, last_updated_at = GETDATE() WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trackingNumber);
            ps.setString(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[CQRS-READ] Error actualizando tracking: " + e.getMessage());
        }
    }

    /**
     * Busca un pedido por ID en el modelo de lectura.
     */
    public OrderReadModel findById(String orderId) {
        String sql = "SELECT order_id, customer_id, status, status_label, total_amount, tracking_number, " +
                     "item_count, items_summary, created_at, last_updated_at, event_count FROM order_read_model WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CQRS-READ] Error buscando: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca pedidos por cliente.
     */
    public List<OrderReadModel> findByCustomer(String customerId, int limit) {
        String sql = "SELECT TOP (?) order_id, customer_id, status, status_label, total_amount, tracking_number, " +
                     "item_count, items_summary, created_at, last_updated_at, event_count FROM order_read_model " +
                     "WHERE customer_id = ? ORDER BY created_at DESC";
        return executeQuery(sql, ps -> {
            ps.setInt(1, limit);
            ps.setString(2, customerId);
        });
    }

    /**
     * Busca pedidos por estado.
     */
    public List<OrderReadModel> findByStatus(String status, int limit) {
        String sql = "SELECT TOP (?) order_id, customer_id, status, status_label, total_amount, tracking_number, " +
                     "item_count, items_summary, created_at, last_updated_at, event_count FROM order_read_model " +
                     "WHERE status = ? ORDER BY created_at DESC";
        return executeQuery(sql, ps -> {
            ps.setInt(1, limit);
            ps.setString(2, status);
        });
    }

    /**
     * Lista todos los pedidos recientes.
     */
    public List<OrderReadModel> findRecent(int limit) {
        String sql = "SELECT TOP (?) order_id, customer_id, status, status_label, total_amount, tracking_number, " +
                     "item_count, items_summary, created_at, last_updated_at, event_count FROM order_read_model " +
                     "ORDER BY created_at DESC";
        return executeQuery(sql, ps -> ps.setInt(1, limit));
    }

    /**
     * Estadisticas del modelo de lectura.
     */
    public String getStats() {
        String sql = "SELECT COUNT(*) as total, " +
                     "SUM(CASE WHEN status = 'CREATED' THEN 1 ELSE 0 END) as created, " +
                     "SUM(CASE WHEN status = 'PAID' THEN 1 ELSE 0 END) as paid, " +
                     "SUM(CASE WHEN status = 'SHIPPED' THEN 1 ELSE 0 END) as shipped, " +
                     "SUM(CASE WHEN status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered, " +
                     "SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled, " +
                     "SUM(total_amount) as revenue FROM order_read_model";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return String.format("{\"total\":%d,\"created\":%d,\"paid\":%d,\"shipped\":%d,\"delivered\":%d,\"cancelled\":%d,\"totalRevenue\":%.2f}",
                    rs.getInt("total"), rs.getInt("created"), rs.getInt("paid"),
                    rs.getInt("shipped"), rs.getInt("delivered"), rs.getInt("cancelled"),
                    rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            System.err.println("[CQRS-READ] Error en stats: " + e.getMessage());
        }
        return "{\"total\":0}";
    }

    @FunctionalInterface
    interface ParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private List<OrderReadModel> executeQuery(String sql, ParamSetter setter) {
        List<OrderReadModel> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CQRS-READ] Error: " + e.getMessage());
        }
        return results;
    }

    private OrderReadModel mapRow(ResultSet rs) throws SQLException {
        return new OrderReadModel(
            rs.getString("order_id"),
            rs.getString("customer_id"),
            rs.getString("status"),
            rs.getString("status_label"),
            rs.getDouble("total_amount"),
            rs.getString("tracking_number"),
            rs.getInt("item_count"),
            rs.getString("items_summary"),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("last_updated_at") != null ? rs.getTimestamp("last_updated_at").toInstant() : null,
            rs.getInt("event_count")
        );
    }
}
