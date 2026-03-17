package com.edacourse.api.catalog.infrastructure.cdc;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.edacourse.api.catalog.domain.event.ProductChangedEvent;

public class NativeCdcStrategy implements CdcStrategy {
    private final String jdbcUrl;
    private final String user;
    private final String password;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private byte[] lastLsn = null;

    public NativeCdcStrategy(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
    }
    

    @Override
    public void start(EventBus eventBus, String topic) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            lastLsn = getMaxLsn(conn);
            System.out.println("[CDC] Starting native CDC strategy with last LSN: " + lastLsn);
        } catch (SQLException e) {
            System.out.println("[CDC] Error getting last LSN: " + e.getMessage());
        }

        scheduler.scheduleAtFixedRate(() -> pollChanges(eventBus, topic), 5, 5, TimeUnit.SECONDS);
    }

    private void pollChanges(EventBus eventBus, String topic) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            byte[] currentLsn = getMaxLsn(conn);

            if (currentLsn == null || lastLsn == null) return;

            String sql = """
                DECLARE @from_lsn binary(10) = ?;
                DECLARE @to_lsn binary(10) = ?;
                IF @from_lsn < @to_lsn
                    SELECT __$operation, id, name, description, price, category, stock, updated_at
                    FROM cdc.fn_cdc_get_all_changes_dbo_products(@from_lsn, @to_lsn, N'all');
                """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBytes(1, lastLsn);
                ps.setBytes(2, currentLsn);

                try (ResultSet rs = ps.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        int operation = rs.getInt("__$operation");
                        if (operation == 2 || operation == 4) {
                            String op = operation == 2 ? "INSERT" : "UPDATE";

                            ProductChangedEvent event = new ProductChangedEvent(
                                rs.getString("id"),
                                rs.getString("name"),
                                rs.getString("description"),
                                rs.getDouble("price"),
                                rs.getString("category"),
                                rs.getInt("stock"),
                                op
                            );

                            eventBus.publish(topic, event);
                            count++;
                            System.out.println("[CDC] Publicado evento: " + event);
                        }
                        if (count > 0) {
                            System.out.println("[CDC] Procesados " + count + " cambios");
                        }
                    }
                }
            }
            lastLsn = currentLsn;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private byte[] getMaxLsn(Connection conn) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement("SELECT sys.fn_cdc_get_max_lsn() AS max_lsn")) {
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return rs.getBytes("max_lsn");
            }
            return null;
        }
    }

    @Override
    public String getName() {
        return "native";
    }

    @Override
    public void close() {
        System.out.println("[CDC] Deteniendo estrategia nativa");
    }
}
        }
    }

    private byte[] getMaxLsn(Connection conn) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement("SELECT sys.fn_cdc_get_max_lsn() AS max_lsn")) {
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return rs.getBytes("max_lsn");
            }
            return null;
        }
    }

    @Override
    public String getName() {
        return "native";
    }

    @Override
    public void close() {
        scheduler.shutdown();
        System.out.println("[CDC] Deteniendo estrategia nativa");
    }
}
