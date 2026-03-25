package com.edacourse.api.backup.infrastructure.persistence;

import com.edacourse.api.backup.domain.port.ProductSeeder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Random;
import java.util.UUID;

public class SqlServerProductSeeder implements ProductSeeder {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final Random random = new Random();

    private static final String[] ADJECTIVES = {
        "Pro", "Ultra", "Max", "Elite", "Plus", "Lite", "Mini", "Mega", "Super", "Turbo",
        "Premium", "Basic", "Advanced", "Classic", "Smart", "Rapido", "Portatil", "Inalambrico"
    };

    private static final String[] PRODUCTS = {
        "Laptop", "Teclado", "Mouse", "Monitor", "Auriculares", "Webcam", "Tablet",
        "Impresora", "Router", "Disco SSD", "Memoria RAM", "Procesador", "Tarjeta Grafica",
        "Cargador", "Cable USB", "Hub USB", "Altavoz", "Microfono", "Silla Gamer", "Escritorio"
    };

    private static final String[] CATEGORIES = {
        "Computadoras", "Perifericos", "Audio", "Redes", "Almacenamiento",
        "Componentes", "Accesorios", "Muebles", "Gaming", "Oficina"
    };

    public SqlServerProductSeeder(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    @Override
    public SeedResult seed(int count) {
        long startTime = System.currentTimeMillis();
        int inserted = 0;
        int batchSize = 500;

        String sql = "INSERT INTO products (id, name, description, price, category, stock) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (int i = 0; i < count; i++) {
                String id = UUID.randomUUID().toString().substring(0, 8);
                String name = randomProduct();
                String description = "Descripcion detallada de " + name + ". Producto de alta calidad para uso profesional y personal.";
                double price = 10 + random.nextDouble() * 1990;
                String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
                int stock = 1 + random.nextInt(500);

                ps.setString(1, id);
                ps.setString(2, name);
                ps.setString(3, description);
                ps.setDouble(4, Math.round(price * 100.0) / 100.0);
                ps.setString(5, category);
                ps.setInt(6, stock);
                ps.addBatch();

                if ((i + 1) % batchSize == 0) {
                    ps.executeBatch();
                    conn.commit();
                    inserted += batchSize;
                    System.out.println("[SEEDER] Insertados " + inserted + "/" + count + " productos...");
                }
            }

            int remaining = count % batchSize;
            if (remaining > 0) {
                ps.executeBatch();
                conn.commit();
                inserted += remaining;
            }

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("[SEEDER] Completado: " + inserted + " productos en " + elapsed + "ms");
            return new SeedResult(inserted, elapsed);

        } catch (Exception e) {
            System.err.println("[SEEDER] Error: " + e.getMessage());
            long elapsed = System.currentTimeMillis() - startTime;
            return new SeedResult(inserted, elapsed);
        }
    }

    private String randomProduct() {
        String product = PRODUCTS[random.nextInt(PRODUCTS.length)];
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        return product + " " + adjective;
    }
}
