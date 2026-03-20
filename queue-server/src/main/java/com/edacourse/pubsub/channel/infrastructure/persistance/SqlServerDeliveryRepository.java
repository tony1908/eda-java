package com.edacourse.pubsub.channel.infrastructure.persistance;

import com.edacourse.pubsub.channel.model.DeliveryRecord;
import com.edacourse.pubsub.channel.repository.DeliveryRespository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SqlServerDeliveryRepository implements DeliveryRespository {
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public SqlServerDeliveryRepository() {
        this.jdbcUrl = System.getenv("DB_URL");
        this.username = System.getenv("DB_USER");
        this.password = System.getenv("DB_PASSWORD");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public void save(DeliveryRecord record) {
        String sql = "INSERT INTO delivery_log (message_id, subscription_id, channel_name, webhook_url, status, http_status, attempt, error_message) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getMessageId());
            ps.setString(2, record.getSubscriptionId());
            ps.setString(3, record.getChannelName());
            ps.setString(4, record.getWebhookUrl());
            ps.setString(5, record.getStatus());
            ps.setInt(6, record.getHttpStatus());
            ps.setInt(7, record.getAttempt());
            ps.setString(8, record.getErrorMessage());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    record.setId(String.valueOf(rs.getLong(1)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando delivery log: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DeliveryRecord> findByChannelName(String channelName, int limit) {
        String sql = "SELECT TOP(?) id, message_id, subscription_id, channel_name, webhook_url, status, http_status, attempt, error_message, delivered_at " +
                     "FROM delivery_log WHERE channel_name = ? ORDER BY id DESC";
        List<DeliveryRecord> records = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setString(2, channelName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRow(rs));
                }
            }
            return records;
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando delivery logs por canal: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DeliveryRecord> findBySubscriptionId(String subscriptionId, int limit) {
        String sql = "SELECT TOP(?) id, message_id, subscription_id, channel_name, webhook_url, status, http_status, attempt, error_message, delivered_at " +
                     "FROM delivery_log WHERE subscription_id = ? ORDER BY id DESC";
        List<DeliveryRecord> records = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setString(2, subscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRow(rs));
                }
            }
            return records;
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando delivery logs por suscripcion: " + e.getMessage(), e);
        }
    }

    private DeliveryRecord mapRow(ResultSet rs) throws SQLException {
        DeliveryRecord record = new DeliveryRecord();
        record.setId(rs.getString("id"));
        record.setMessageId(rs.getString("message_id"));
        record.setSubscriptionId(rs.getString("subscription_id"));
        record.setChannelName(rs.getString("channel_name"));
        record.setWebhookUrl(rs.getString("webhook_url"));
        record.setStatus(rs.getString("status"));
        record.setHttpStatus(rs.getInt("http_status"));
        record.setAttempt(rs.getInt("attempt"));
        record.setErrorMessage(rs.getString("error_message"));
        Timestamp ts = rs.getTimestamp("delivered_at");
        if (ts != null) {
            record.setDeliveredAt(ts.toInstant());
        }
        return record;
    }
}
