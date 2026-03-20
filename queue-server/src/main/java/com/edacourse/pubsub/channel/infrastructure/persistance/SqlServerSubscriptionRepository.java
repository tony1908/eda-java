package com.edacourse.pubsub.channel.infrastructure.persistance;

import com.edacourse.pubsub.channel.model.Subscription;
import com.edacourse.pubsub.channel.repository.SubscriptionRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlServerSubscriptionRepository implements SubscriptionRepository {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public SqlServerSubscriptionRepository() {
        this.jdbcUrl = System.getenv("DB_URL");
        this.username = System.getenv("DB_USER");
        this.password = System.getenv("DB_PASSWORD");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public Subscription save(Subscription subscription) {
        String sql = "INSERT INTO subscriptions (id, channel_id, webhook_url, secret, description) VALUES (?, ?, ?, ?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subscription.getId());
            ps.setString(2, subscription.getChannelId());
            ps.setString(3, subscription.getWebhookUrl());
            ps.setString(4, subscription.getSecret());
            ps.setString(5, subscription.getDescription());
            ps.executeUpdate();

            subscription.setActive(true);

            System.out.println("[PUBSUB] Suscripción guardada: id=" + subscription.getId() + " canal=" + subscription.getChannelId());
            return subscription;
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando suscripción: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Subscription> findById(String id) {
        String sql = "SELECT id, channel_id, webhook_url, secret, description, active, created_at FROM subscriptions WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando suscripción: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Subscription> findByChannelId(String channelId) {
        String sql = "SELECT id, channel_id, webhook_url, secret, description, active, created_at FROM subscriptions WHERE channel_id = ?";
        return queryByParams(sql, channelId);
    }

    public List<Subscription> findActiveByChannelId(String channelId) {
        String sql = "SELECT id, channel_id, webhook_url, secret, description, active, created_at FROM subscriptions WHERE channel_id = ? AND active = 1";
        return queryByParams(sql, channelId);
    }

    public List<Subscription> findActiveByChannelName(String channelName) {
        String sql = "SELECT id, channel_id, webhook_url, secret, description, active, created_at FROM subscriptions WHERE channel_name = ? AND active = 1";
        return queryByParams(sql, channelName);
    }

    @Override
    public void deactivate(String id) {
        String sql = "UPDATE subscriptions SET active = 0 WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Suscripcion no encontrada: " + id);
            }
            System.out.println("[PUBSUB] Suscripcion desactivada: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Error desactivando suscripcion: " + e.getMessage(), e);
        }
    }

    private List<Subscription> queryByParams(String sql, String param) {
        List<Subscription> subscriptions = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    subscriptions.add(mapRow(rs));
                }
            }
            return subscriptions;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando suscripciones: " + e.getMessage(), e);
        }
    }

    private Subscription mapRow(ResultSet rs) throws SQLException {
        return new Subscription(
            rs.getString("id"),
            rs.getString("channel_id"),
            rs.getString("webhook_url"),
            rs.getString("secret"),
            rs.getString("description"),
            rs.getBoolean("active"),
            rs.getTimestamp("created_at").toInstant()
        );
    }

}
