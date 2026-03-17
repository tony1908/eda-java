package com.edacourse.api.catalog.infrastructure.cdc;

public class PollingCdcStrategy implements CdcStrategy {
    private final String jdbcUrl;
    private final String user;
    private final String password;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private Timestamp lastPollTime;

    public PollingCdcStrategy(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
    }

    @Override
    public void start(EventBus eventBus, String topic) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            PreparedStatement ps = conn.prepareStatement("SELECT GETDATE() AS now");
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                lastPollTime = rs.getTimestamp("now");
            }
            System.out.println("[CDC] Starting polling CDC strategy with last poll time: " + lastPollTime);
        } catch (SQLException e) {
            System.out.println("[CDC] Error getting last poll time: " + e.getMessage());
            lastPollTime = new Timestamp(System.currentTimeMillis());
        }

        scheduler.scheduleAtFixedRate(() -> pollChanges(eventBus, topic), 5, 5, TimeUnit.SECONDS);
    }
    

    private void pollChanges(EventBus eventBus, String topic) {
        String sql = "SELECT id, name, description, price, category, stock, created_at, updated_at " +
                     "FROM products WHERE updated_at > ? ORDER BY updated_at ASC";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, lastPollTime);
            Timestamp newLastPoll = lastPollTime;
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    String productId = rs.getString("id");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    double price = rs.getDouble("price");
                    String category = rs.getString("category");
                    int stock = rs.getInt("stock");
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    Timestamp createdAt = rs.getTimestamp("created_at");

                    String operation = createdAt.equals(updatedAt) ? "INSERT" : "UPDATE";
                    

                    ProductChangedEvent event = new ProductChangedEvent(
                        productId,
                        name,
                        description,
                        price,
                        category,
                        stock,
                        operation
                    );

                    eventBus.publish(topic, event);
                    count++;
                    System.out.println("[CDC] Publicado evento: " + event);
                    if (updatedAt.after(newLastPoll)) {
                        newLastPoll = updatedAt;
                    }
                }

                if (count > 0) {
                    System.out.println("[CDC] Procesados " + count + " cambios");
                }
            }

            lastPollTime = newLastPoll;
        } catch (SQLException e) {
            System.err.println("[CDC] Error al hacer polling de cambios: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "polling";
    }

    @Override
    public void close() {
        scheduler.shutdown();
        System.out.println("[CDC] Deteniendo estrategia polling");
    }
}
