# Backup Ports Refactor — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Extract direct JDBC calls from BackupService and DataSeeder into dedicated port interfaces with SQL Server implementations, following Clean Architecture.

**Architecture:** Create two domain port interfaces (`ProductExporter`, `ProductSeeder`) in the backup bounded context. Move existing JDBC logic into infrastructure implementations (`SqlServerProductExporter`, `SqlServerProductSeeder`). Update BackupService, DataSeeder (remove), BackupResource, AppBinder, and Application.java to wire everything through the ports.

**Tech Stack:** Java 21, Jersey 3.1.9, JDBC (mssql-jdbc), HK2 DI (via AppBinder)

---

### Task 1: Create ProductExporter port interface

**Files:**
- Create: `src/main/java/com/edacourse/api/backup/domain/port/ProductExporter.java`

**Step 1: Create the interface**

```java
package com.edacourse.api.backup.domain.port;

public interface ProductExporter {
    int exportToJson(String filePath) throws Exception;
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/backup/domain/port/ProductExporter.java
git commit -m "feat(backup): add ProductExporter port interface"
```

---

### Task 2: Create SqlServerProductExporter implementation

**Files:**
- Create: `src/main/java/com/edacourse/api/backup/infrastructure/persistence/SqlServerProductExporter.java`

**Step 1: Create the implementation**

Move the JDBC export logic from `BackupService.exportProductsToJson()` (lines 68-92) and the `escapeJson()` helper into this class. The DB connection parameters come via constructor.

```java
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
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/backup/infrastructure/persistence/SqlServerProductExporter.java
git commit -m "feat(backup): add SqlServerProductExporter implementation"
```

---

### Task 3: Create ProductSeeder port interface

**Files:**
- Create: `src/main/java/com/edacourse/api/backup/domain/port/ProductSeeder.java`

**Step 1: Create the interface**

```java
package com.edacourse.api.backup.domain.port;

public interface ProductSeeder {
    SeedResult seed(int count);

    record SeedResult(int inserted, long elapsedMs) {}
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/backup/domain/port/ProductSeeder.java
git commit -m "feat(backup): add ProductSeeder port interface"
```

---

### Task 4: Create SqlServerProductSeeder implementation

**Files:**
- Create: `src/main/java/com/edacourse/api/backup/infrastructure/persistence/SqlServerProductSeeder.java`

**Step 1: Create the implementation**

Move all logic from `DataSeeder` (the ADJECTIVES, PRODUCTS, CATEGORIES arrays, batch insert logic) into this class.

```java
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
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/backup/infrastructure/persistence/SqlServerProductSeeder.java
git commit -m "feat(backup): add SqlServerProductSeeder implementation"
```

---

### Task 5: Refactor BackupService to use ProductExporter port

**Files:**
- Modify: `src/main/java/com/edacourse/api/backup/application/BackupService.java`

**Step 1: Update BackupService**

- Remove fields: `dbUrl`, `dbUser`, `dbPassword`
- Remove methods: `exportProductsToJson()`, `escapeJson()`
- Add field: `ProductExporter productExporter`
- Update constructor to accept `ProductExporter`
- Update `executeBackup()` to call `productExporter.exportToJson(exportFile)`

The updated class:

```java
package com.edacourse.api.backup.application;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.backup.infrastructure.restic.ResticClient;
import com.edacourse.api.backup.domain.port.ProductExporter;
import com.edacourse.api.backup.domain.event.*;

import java.io.*;
import java.util.UUID;

public class BackupService {
    private final EventBus eventBus;
    private final ResticClient resticClient;
    private final ProductExporter productExporter;
    private final String exportDir;

    public BackupService(EventBus eventBus, ResticClient resticClient, ProductExporter productExporter) {
        this.eventBus = eventBus;
        this.resticClient = resticClient;
        this.productExporter = productExporter;
        this.exportDir = System.getenv().getOrDefault("EXPORT_DIR", "/mnt/backups");

        new File(exportDir).mkdirs();
        System.out.println("[BACKUP] Servicio inicializado. Directorio de exportacion: " + exportDir);
    }

    public String requestBackup(String description) {
        String backupId = "bk_" + UUID.randomUUID().toString().substring(0, 8);
        eventBus.publish("backup.requested", new BackupRequestedEvent(backupId, description));
        return backupId;
    }

    public void executeBackup(String backupId, String description) {
        long startTime = System.currentTimeMillis();
        try {
            cleanExportDir();
            String exportFile = exportDir + "/products_" + backupId + ".json";
            int count = productExporter.exportToJson(exportFile);
            System.out.println("[BACKUP] " + count + " productos exportados a " + exportFile);

            String snapshotId = resticClient.backup(exportFile);

            if (snapshotId != null) {
                long duration = System.currentTimeMillis() - startTime;
                long fileSize = new File(exportFile).length();
                eventBus.publish("backup.completed",
                    new BackupCompletedEvent(backupId, snapshotId, fileSize, duration));
                System.out.println("[BACKUP] Completado en " + duration + "ms. Snapshot: " + snapshotId);
            } else {
                eventBus.publish("backup.failed",
                    new BackupFailedEvent(backupId, "Restic backup retorno null"));
            }

        } catch (Exception e) {
            eventBus.publish("backup.failed",
                new BackupFailedEvent(backupId, "Error al exportar: " + e.getMessage()));
        }
    }

    public String requestRestore(String snapshotId) {
        String restoreId = "rs_" + UUID.randomUUID().toString().substring(0, 8);
        eventBus.publish("restore.requested",
            new RestoreRequestedEvent(restoreId, snapshotId));
        return restoreId;
    }

    public void executeRestore(String restoreId, String snapshotId) {
        long startTime = System.currentTimeMillis();
        String restoreDir = exportDir + "/../restores/restore_" + restoreId;
        try {
            boolean success = resticClient.restore(snapshotId, restoreDir);
            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                File dir = new File(restoreDir);
                int fileCount = countFiles(dir);
                eventBus.publish("restore.completed",
                    new RestoreCompletedEvent(restoreId, snapshotId, fileCount, duration));
                System.out.println("[BACKUP] Restauracion completada: " + fileCount + " archivos en " + duration + "ms");
            } else {
                eventBus.publish("restore.failed",
                    new RestoreFailedEvent(restoreId, "Restic restore fallo"));
                System.out.println("[BACKUP] Restauracion fallida");
            }

        } catch (Exception e) {
            eventBus.publish("restore.failed",
                new RestoreFailedEvent(restoreId, "Error al restaurar: " + e.getMessage()));
        }
    }

    public String getSnapshots() {
        return resticClient.listSnapshots();
    }

    public String getStats() {
        return resticClient.stats();
    }

    private void cleanExportDir() {
        File dir = new File(exportDir);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) f.delete();
            }
        }
    }

    private int countFiles(File dir) {
        if (!dir.exists()) return 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        int count = 0;
        for (File f : files) {
            if (f.isFile()) count++;
            else count += countFiles(f);
        }
        return count;
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/backup/application/BackupService.java
git commit -m "refactor(backup): use ProductExporter port instead of direct JDBC"
```

---

### Task 6: Refactor BackupResource to use ProductSeeder port and delete DataSeeder

**Files:**
- Modify: `src/main/java/com/edacourse/api/backup/interfaces/BackupResource.java`
- Delete: `src/main/java/com/edacourse/api/backup/application/DataSeeder.java`

**Step 1: Update BackupResource**

Replace `DataSeeder` injection with `ProductSeeder`. Update the seed endpoint to use the port interface.

```java
package com.edacourse.api.backup.interfaces;

import com.edacourse.api.backup.application.BackupService;
import com.edacourse.api.backup.domain.port.ProductSeeder;
import com.edacourse.api.backup.domain.port.ProductSeeder.SeedResult;
import com.edacourse.api.backup.domain.dto.BackupRequestDTO;
import com.edacourse.api.backup.domain.dto.RestoreRequestDTO;
import com.edacourse.api.backup.domain.dto.BackupResponseDTO;
import com.edacourse.api.backup.domain.dto.RestoreResponseDTO;
import jakarta.inject.Inject;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/backups")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BackupResource {

    @Inject
    private BackupService backupService;

    @Inject
    private ProductSeeder productSeeder;

    @POST
    @Path("/request")
    public Response requestBackup(BackupRequestDTO request) {
        String backupId = backupService.requestBackup(request.getDescription());
        return Response.ok(new BackupResponseDTO(backupId, "Backup requested")).build();
    }

    @POST
    @Path("/restore")
    public Response requestRestore(RestoreRequestDTO request) {
        String restoreId = backupService.requestRestore(request.getSnapshotId());
        return Response.ok(new RestoreResponseDTO(restoreId, "Restore requested")).build();
    }

    @GET
    @Path("/snapshots")
    public Response listSnapshots() {
        String snapshots = backupService.getSnapshots();
        return Response.ok(snapshots).build();
    }

    @GET
    @Path("/stats")
    public Response getStats() {
        String stats = backupService.getStats();
        return Response.ok(stats).build();
    }

    @POST
    @Path("/seed")
    public Response seedData(@QueryParam("count") @DefaultValue("10000") int count) {
        SeedResult result = productSeeder.seed(count);
        return Response.ok(result).build();
    }
}
```

**Step 2: Delete DataSeeder**

```bash
rm src/main/java/com/edacourse/api/backup/application/DataSeeder.java
```

**Step 3: Commit**

```bash
git add src/main/java/com/edacourse/api/backup/interfaces/BackupResource.java
git rm src/main/java/com/edacourse/api/backup/application/DataSeeder.java
git commit -m "refactor(backup): use ProductSeeder port, remove DataSeeder"
```

---

### Task 7: Update DI wiring (AppBinder + Application.java)

**Files:**
- Modify: `src/main/java/com/edacourse/api/shared/config/AppBinder.java`
- Modify: `src/main/java/com/edacourse/api/Application.java`

**Step 1: Update Application.java**

After creating `ResticClient` (line 130), instantiate the SQL implementations and pass `ProductExporter` to `BackupService`:

```java
// Replace lines 132-133:
//   BackupService backupService = new BackupService(eventBus, resticClient);
// With:
String dbUrl = System.getenv().getOrDefault("SQLSERVER_URL", "jdbc:sqlserver://sqlserver:1433;databaseName=eventflow;encrypt=false");
String dbUser = System.getenv().getOrDefault("SQLSERVER_USER", "sa");
String dbPassword = System.getenv().getOrDefault("SQLSERVER_PASSWORD", "EventFlow123!");

ProductExporter productExporter = new SqlServerProductExporter(dbUrl, dbUser, dbPassword);
ProductSeeder productSeeder = new SqlServerProductSeeder(dbUrl, dbUser, dbPassword);

BackupService backupService = new BackupService(eventBus, resticClient, productExporter);
```

Update the `AppBinder` constructor call (line 145) to also pass `productSeeder`:

```java
new AppBinder(serializer, eventBus, sseResource, catalogService, searchService, sseBroadcaster, backupService, productSeeder)
```

Remove `.register(DataSeeder.class)` from line 148.

Add these imports:
```java
import com.edacourse.api.backup.domain.port.ProductExporter;
import com.edacourse.api.backup.domain.port.ProductSeeder;
import com.edacourse.api.backup.infrastructure.persistence.SqlServerProductExporter;
import com.edacourse.api.backup.infrastructure.persistence.SqlServerProductSeeder;
```

**Step 2: Update AppBinder**

- Remove `DataSeeder` import
- Add `ProductSeeder` field and constructor param
- Replace `bind(DataSeeder.class).to(DataSeeder.class).in(Singleton.class)` with `bind(productSeeder).to(ProductSeeder.class)`

```java
package com.edacourse.api.shared.config;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.shared.infrastructure.serialization.EventSerializer;
import com.edacourse.api.order.domain.repository.OrderRepository;
import com.edacourse.api.order.infrastructure.persistence.InMemoryOrderRepository;
import com.edacourse.api.order.application.service.OrderService;
import com.edacourse.api.order.interfaces.sse.OrderSseResource;
import com.edacourse.api.catalog.application.service.CatalogService;
import com.edacourse.api.search.application.service.SearchService;
import com.edacourse.api.backup.application.BackupService;
import com.edacourse.api.backup.domain.port.ProductSeeder;
import jakarta.inject.Singleton;
import com.edacourse.api.shared.infrastructure.sse.EventSseBroadcaster;

public class AppBinder extends AbstractBinder {
    private final EventSerializer serializer;
    private final EventBus eventBus;
    private final OrderSseResource sseResource;
    private final CatalogService catalogService;
    private final SearchService searchService;
    private final EventSseBroadcaster eventSseBroadcaster;
    private final BackupService backupService;
    private final ProductSeeder productSeeder;

    public AppBinder(EventSerializer serializer,
        EventBus eventBus,
        OrderSseResource sseResource,
        CatalogService catalogService,
        SearchService searchService,
        EventSseBroadcaster eventSseBroadcaster,
        BackupService backupService,
        ProductSeeder productSeeder
    ) {
        this.serializer = serializer;
        this.eventBus = eventBus;
        this.sseResource = sseResource;
        this.catalogService = catalogService;
        this.searchService = searchService;
        this.eventSseBroadcaster = eventSseBroadcaster;
        this.backupService = backupService;
        this.productSeeder = productSeeder;
    }

    @Override
    protected void configure() {
        bind(serializer).to(EventSerializer.class);
        bind(eventBus).to(EventBus.class);

        bind(InMemoryOrderRepository.class).to(OrderRepository.class).in(Singleton.class);
        bind(OrderService.class).to(OrderService.class).in(Singleton.class);
        bind(sseResource).to(OrderSseResource.class).in(Singleton.class);
        bind(catalogService).to(CatalogService.class);

        bind(searchService).to(SearchService.class);
        bind(eventSseBroadcaster).to(EventSseBroadcaster.class);

        bind(backupService).to(BackupService.class);
        bind(productSeeder).to(ProductSeeder.class);
    }
}
```

**Step 3: Commit**

```bash
git add src/main/java/com/edacourse/api/Application.java src/main/java/com/edacourse/api/shared/config/AppBinder.java
git commit -m "refactor(backup): wire ProductExporter and ProductSeeder through DI"
```

---

### Task 8: Build and verify

**Step 1: Run Maven build**

```bash
cd eventflow && mvn clean compile
```

Expected: BUILD SUCCESS with no compilation errors.

**Step 2: If errors, fix and re-run until clean**

**Step 3: Commit any fixes if needed**
