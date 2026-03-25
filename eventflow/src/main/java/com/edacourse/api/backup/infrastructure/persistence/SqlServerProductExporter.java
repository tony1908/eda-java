package com.edacourse.api.backup.infrastructure.persistence;

import com.edacourse.api.backup.domain.port.ProductExporter;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;

public class SqlServerProductExporter implements ProductExporter {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public SqlServerProductExporter(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    @Override
    public int exportToJson(String filePath) throws Exception {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, description, price, category, stock FROM products");
             PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            writer.println("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) writer.println(",");
                first = false;
                writer.printf("  {\"id\":\"%s\",\"name\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"category\":\"%s\",\"stock\":%d}",
                    rs.getString("id"),
                    escapeJson(rs.getString("name")),
                    escapeJson(rs.getString("description")),
                    rs.getDouble("price"),
                    escapeJson(rs.getString("category")),
                    rs.getInt("stock"));
                count++;
            }
            writer.println("\n]");
        }
        return count;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
