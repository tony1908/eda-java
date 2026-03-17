package com.edacourse.api.catalog.infrastructure.cdc;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.edacourse.api.catalog.domain.event.ProductChangedEvent;

public class TriggerOutboxStrategy implements CdcStrategy {
    
    private final String jdbcUrl;
    private final String user;
    private final String password;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    public TriggerOutboxStrategy(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
    }

    @Override
    public void start(EventBus eventBus, String topic) {
        scheduler.scheduleAtFixedRate(() -> pollOutbox(eventBus, topic), 5, 5, TimeUnit.SECONDS);
    }

    private void pollOutbox(EventBus eventBus, String topic) {
        String selectSql = "SELECT o.id AS outbox_id, o.product_id, o.operation, " +
                          "p.name, p.description, p.price, p.category, p.stock " +
                          "FROM product_outbox o " +
                          "JOIN products p ON o.product_id = p.id " +
                          "WHERE o.processed = 0 ORDER BY o.id ASC";
        String updateSql = "UPDATE product_outbox SET processed = 1 WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            conn.setAutoCommit(false);
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);

                ResultSet rs = selectStmt.executeQuery();
                int count = 0;
                while (rs.next()) {
                    long outboxId = rs.getLong("outbox_id");
                    String operation = rs.getString("operation");

                    ProductChangedEvent event = new ProductChangedEvent(
                        rs.getString("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getInt("stock"),
                        operation
                    );

                    eventBus.publish(topic, event);
                    updateStmt.setLong(1, outboxId);
                    updateStmt.executeUpdate();
                    count++;
                    System.out.println("[CDC] Publicado evento: " + event);
                }
                conn.commit();
                if (count > 0) {
                    System.out.println("[CDC] Procesados " + count + " cambios");
                }
            }
        } catch (SQLException e) {
            System.err.println("[CDC] Error al hacer polling de cambios: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "trigger-outbox";
    }

    @Override
    public void close() {
        scheduler.shutdown();
        System.out.println("[CDC] Deteniendo estrategia trigger-outbox");
    }
}
