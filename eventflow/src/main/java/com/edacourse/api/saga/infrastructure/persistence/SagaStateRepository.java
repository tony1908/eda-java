package com.edacourse.api.saga.infrastructure.persistence;

import com.edacourse.api.saga.domain.model.SagaState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Persiste el estado de las sagas en SQL Server.
 */
public class SagaStateRepository {

    private final String dbUrl, dbUser, dbPassword;

    public SagaStateRepository() {
        this.dbUrl = System.getenv("SQLSERVER_URL");
        this.dbUser = System.getenv("SQLSERVER_USER");
        this.dbPassword = System.getenv("SQLSERVER_PASSWORD");
        System.out.println("[SAGA-REPO] Repositorio de sagas inicializado");
    }

    public void save(SagaState saga) {
        String sql = "MERGE saga_state AS target " +
                     "USING (SELECT ? AS saga_id) AS source ON target.saga_id = source.saga_id " +
                     "WHEN MATCHED THEN UPDATE SET status=?, current_step=?, completed_steps=?, failure_reason=?, completed_at=? " +
                     "WHEN NOT MATCHED THEN INSERT (saga_id, order_id, status, current_step, completed_steps) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String stepsJson = String.join(",", saga.getCompletedSteps());
            // USING
            ps.setString(1, saga.getSagaId());
            // WHEN MATCHED
            ps.setString(2, saga.getStatus());
            ps.setString(3, saga.getCurrentStep());
            ps.setString(4, stepsJson);
            ps.setString(5, saga.getFailureReason());
            ps.setTimestamp(6, saga.getCompletedAt() != null ? Timestamp.from(saga.getCompletedAt()) : null);
            // WHEN NOT MATCHED
            ps.setString(7, saga.getSagaId());
            ps.setString(8, saga.getOrderId());
            ps.setString(9, saga.getStatus());
            ps.setString(10, saga.getCurrentStep());
            ps.setString(11, stepsJson);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SAGA-REPO] Error guardando saga: " + e.getMessage());
        }
    }

    public SagaState findById(String sagaId) {
        String sql = "SELECT saga_id, order_id, status, current_step, completed_steps, failure_reason, started_at, completed_at FROM saga_state WHERE saga_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sagaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SagaState saga = new SagaState();
                    saga.setSagaId(rs.getString("saga_id"));
                    saga.setOrderId(rs.getString("order_id"));
                    saga.setStatus(rs.getString("status"));
                    saga.setCurrentStep(rs.getString("current_step"));
                    String steps = rs.getString("completed_steps");
                    if (steps != null && !steps.isBlank()) {
                        saga.setCompletedSteps(new ArrayList<>(List.of(steps.split(","))));
                    }
                    saga.setFailureReason(rs.getString("failure_reason"));
                    saga.setStartedAt(rs.getTimestamp("started_at").toInstant());
                    Timestamp completedAt = rs.getTimestamp("completed_at");
                    if (completedAt != null) saga.setCompletedAt(completedAt.toInstant());
                    return saga;
                }
            }
        } catch (SQLException e) {
            System.err.println("[SAGA-REPO] Error: " + e.getMessage());
        }
        return null;
    }

    public List<SagaState> findRecent(int limit) {
        String sql = "SELECT TOP (?) saga_id, order_id, status, current_step, completed_steps, failure_reason, started_at, completed_at FROM saga_state ORDER BY started_at DESC";
        List<SagaState> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SagaState saga = new SagaState();
                    saga.setSagaId(rs.getString("saga_id"));
                    saga.setOrderId(rs.getString("order_id"));
                    saga.setStatus(rs.getString("status"));
                    saga.setCurrentStep(rs.getString("current_step"));
                    String steps = rs.getString("completed_steps");
                    if (steps != null && !steps.isBlank()) {
                        saga.setCompletedSteps(new ArrayList<>(List.of(steps.split(","))));
                    }
                    saga.setFailureReason(rs.getString("failure_reason"));
                    saga.setStartedAt(rs.getTimestamp("started_at").toInstant());
                    Timestamp completedAt = rs.getTimestamp("completed_at");
                    if (completedAt != null) saga.setCompletedAt(completedAt.toInstant());
                    results.add(saga);
                }
            }
        } catch (SQLException e) {
            System.err.println("[SAGA-REPO] Error: " + e.getMessage());
        }
        return results;
    }
}
