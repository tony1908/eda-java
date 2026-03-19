package com.edacourse.pubsub.channel.infrastructure.persistance;

import com.edacourse.pubsub.channel.model.Channel;
import com.edacourse.pubsub.channel.repository.ChannelRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.Instant;

public class SqlServerChannelRepository implements ChannelRepository {
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public SqlServerChannelRepository() {
        this.jdbcUrl = System.getenv("DB_URL");
        this.username = System.getenv("DB_USER");
        this.password = System.getenv("DB_PASSWORD");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public Channel save(Channel channel) {
        String sql = "INSERT INTO channels (id, name, description) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, channel.getId());
            stmt.setString(2, channel.getName());
            stmt.setString(3, channel.getDescription());
            stmt.executeUpdate();
            return channel;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving channel", e);
        }
    }

    @Override
    public Optional<Channel> findById(String id) {
        String sql = "SELECT * FROM channels WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToChannel(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding channel by ID", e);
        }
    }

    @Override
    public Optional<Channel> findByName(String name) {
        String sql = "SELECT * FROM channels WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToChannel(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            System.out.println("Error finding channel by name: " + e.getMessage());
            //throw new RuntimeException("Error finding channel by name", e);
            return Optional.empty();
        }
    }

    @Override
    public List<Channel> findAll() {
        List<Channel> channels = new ArrayList<>();
        String sql = "SELECT * FROM channels";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                channels.add(mapResultSetToChannel(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all channels", e);
        }
        return channels;
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM channels WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting channel", e);
        }
    }

    private Channel mapResultSetToChannel(ResultSet rs) throws SQLException {
        Channel channel = new Channel();
        channel.setId(rs.getString("id"));
        channel.setName(rs.getString("name"));
        channel.setDescription(rs.getString("description"));
        channel.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        return channel;
    }
}