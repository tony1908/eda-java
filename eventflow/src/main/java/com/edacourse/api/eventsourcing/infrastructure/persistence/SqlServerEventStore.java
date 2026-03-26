package com.edacourse.api.eventsourcing.infrastructure.persistence;

import com.edacourse.api.eventsourcing.domain.model.StoredEvent;
import com.edacourse.api.eventsourcing.domain.model.OrderSnapshot;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SqlServerEventStore {

    private final String dbUrl, dbUser, dbPassword;

    public SqlServerEventStore() {
        this.dbUrl = System.getenv("SQLSERVER_URL");
        this.dbUser = System.getenv("SQLSERVER_USER");
        this.dbPassword = System.getenv("SQLSERVER_PASSWORD");
        System.out.println("[EVENT-STORE] Inicializado contra SQL Server (con concurrencia optimista)");
    }

    public long append(String streamId, String aggregateType, String eventType,
                       String payload, int expectedVersion) {
        return append(streamId, aggregateType, eventType, payload, expectedVersion, "{}");
    }

    public long append(String streamId, String aggregateType, String eventType,
                       String payload, int expectedVersion, String metadata) {
        int streamPosition = expectedVersion + 1;
        String sql = "INSERT INTO event_store (stream_id, stream_position, aggregate_type, event_type, payload, metadata) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, streamId);
            ps.setInt(2, streamPosition);
            ps.setString(3, aggregateType);
            ps.setString(4, eventType);
            ps.setString(5, payload);
            ps.setString(6, metadata);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().contains("uq_stream_position") || e.getErrorCode() == 2627)) {
                throw new ConcurrentModificationException(
                    "Conflicto de concurrencia en stream " + streamId +
                    " en posicion " + streamPosition);
            }
            System.err.println("[EVENT-STORE] Error al persistir evento: " + e.getMessage());
        }
        return -1;
    }

    public void append(String streamId, String aggregateType, String eventType, String payload) {
        int currentVersion = getCurrentVersion(streamId);
        append(streamId, aggregateType, eventType, payload, currentVersion);
    }

    public int getCurrentVersion(String streamId) {
        String sql = "SELECT ISNULL(MAX(stream_position), 0) FROM event_store WHERE stream_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, streamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[EVENT-STORE] Error al obtener version: " + e.getMessage());
        }
        return 0;
    }

    public List<StoredEvent> getEvents(String streamId) {
        String sql = "SELECT global_position, stream_id, aggregate_type, event_type, payload, occurred_at " +
                     "FROM event_store WHERE stream_id = ? ORDER BY stream_position ASC";
        List<StoredEvent> events = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, streamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(new StoredEvent(
                        rs.getLong("global_position"),
                        rs.getString("stream_id"),
                        rs.getString("aggregate_type"),
                        rs.getString("event_type"),
                        rs.getString("payload"),
                        rs.getTimestamp("occurred_at").toInstant()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[EVENT-STORE] Error al leer eventos: " + e.getMessage());
        }
        return events;
    }

    public List<StoredEvent> getAllEvents(String aggregateType) {
        String sql = "SELECT global_position, stream_id, aggregate_type, event_type, payload, occurred_at " +
                     "FROM event_store WHERE aggregate_type = ? ORDER BY global_position ASC";
        List<StoredEvent> events = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, aggregateType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(new StoredEvent(
                        rs.getLong("global_position"),
                        rs.getString("stream_id"),
                        rs.getString("aggregate_type"),
                        rs.getString("event_type"),
                        rs.getString("payload"),
                        rs.getTimestamp("occurred_at").toInstant()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[EVENT-STORE] Error: " + e.getMessage());
        }
        return events;
    }

    public void saveSnapshot(OrderSnapshot snapshot) {
        String sql = "MERGE order_snapshots AS target " +
                     "USING (SELECT ? AS order_id) AS source ON target.order_id = source.order_id " +
                     "WHEN MATCHED THEN UPDATE SET status=?, customer_id=?, total_amount=?, tracking_number=?, version=?, snapshot_at=GETDATE() " +
                     "WHEN NOT MATCHED THEN INSERT (order_id, status, customer_id, total_amount, tracking_number, version) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, snapshot.orderId());
            ps.setString(2, snapshot.status());
            ps.setString(3, snapshot.customerId());
            ps.setDouble(4, snapshot.totalAmount());
            ps.setString(5, snapshot.trackingNumber());
            ps.setInt(6, snapshot.version());
            ps.setString(7, snapshot.orderId());
            ps.setString(8, snapshot.status());
            ps.setString(9, snapshot.customerId());
            ps.setDouble(10, snapshot.totalAmount());
            ps.setString(11, snapshot.trackingNumber());
            ps.setInt(12, snapshot.version());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[EVENT-STORE] Error al guardar snapshot: " + e.getMessage());
        }
    }

    public OrderSnapshot loadSnapshot(String orderId) {
        String sql = "SELECT order_id, status, customer_id, total_amount, tracking_number, version, snapshot_at " +
                     "FROM order_snapshots WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new OrderSnapshot(
                        rs.getString("order_id"),
                        rs.getString("status"),
                        rs.getString("customer_id"),
                        rs.getDouble("total_amount"),
                        rs.getString("tracking_number"),
                        rs.getInt("version"),
                        rs.getTimestamp("snapshot_at").toInstant()
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[EVENT-STORE] Error al cargar snapshot: " + e.getMessage());
        }
        return null;
    }

    public List<StoredEvent> getEventsAfter(String streamId, long afterGlobalPosition) {
        String sql = "SELECT global_position, stream_id, aggregate_type, event_type, payload, occurred_at " +
                     "FROM event_store WHERE stream_id = ? AND global_position > ? ORDER BY stream_position ASC";
        List<StoredEvent> events = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, streamId);
            ps.setLong(2, afterGlobalPosition);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(new StoredEvent(
                        rs.getLong("global_position"),
                        rs.getString("stream_id"),
                        rs.getString("aggregate_type"),
                        rs.getString("event_type"),
                        rs.getString("payload"),
                        rs.getTimestamp("occurred_at").toInstant()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[EVENT-STORE] Error: " + e.getMessage());
        }
        return events;
    }

    public static class ConcurrentModificationException extends RuntimeException {
        public ConcurrentModificationException(String message) {
            super(message);
        }
    }
}
