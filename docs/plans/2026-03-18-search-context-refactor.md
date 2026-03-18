# Search Context Refactor — Domain Model & Clean Architecture

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Refactor the Search bounded context so OpenSearch is an infrastructure detail hidden behind a domain-oriented repository. Add proper domain value objects, application DTOs, and stop leaking raw OpenSearch JSON to API consumers.

**Architecture:** Introduce `SearchableProduct` (domain value object for what gets indexed), `SearchResult` (domain value object for what comes back), a domain-oriented `ProductSearchRepository` interface, and a `SearchResultResponse` DTO for the REST layer. The `OpenSearchProductSearchRepository` impl owns all JSON/query DSL construction internally. `SearchService` works only with domain types.

**Tech Stack:** Java 21, Jersey/Grizzly, OpenSearch 2.19, Jackson

---

## Current State (what changes)

```
interfaces/rest/SearchResource          → returns raw OpenSearch JSON (String)
application/service/SearchService       → builds OpenSearch query DSL, returns String
infrastructure/opensearch/SearchRepository       → generic HTTP client (get/put/post/delete)
infrastructure/opensearch/OpenSearchRepository   → generic HTTP impl
```

## Target State

```
domain/model/SearchableProduct                              → value object for indexing
domain/model/SearchResult                                   → value object for query results
domain/repository/ProductSearchRepository                   → domain-oriented interface
application/dto/SearchResultResponse                        → REST response DTO
application/service/SearchService                           → works with domain types only
infrastructure/opensearch/OpenSearchProductSearchRepository → implements domain repository, owns all JSON/query DSL
infrastructure/opensearch/EmbeddingGenerator                → unchanged (already behind interface)
infrastructure/opensearch/TrigramEmbeddingGenerator         → unchanged
infrastructure/subscriber/SearchSubscriber                  → minimal change (builds SearchableProduct)
interfaces/rest/SearchResource                              → returns List<SearchResultResponse>
```

---

### Task 1: Create `SearchableProduct` domain value object

**Files:**
- Create: `src/main/java/com/edacourse/api/search/domain/model/SearchableProduct.java`

**Step 1: Create the value object**

```java
package com.edacourse.api.search.domain.model;

import java.time.Instant;

public record SearchableProduct(
    String productId,
    String name,
    String description,
    double price,
    String category,
    int stock,
    Instant indexedAt
) {
    public String textForEmbedding() {
        return name + " " + description + " " + category;
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/search/domain/model/SearchableProduct.java
git commit -m "feat(search): add SearchableProduct domain value object"
```

---

### Task 2: Create `SearchResult` domain value object

**Files:**
- Create: `src/main/java/com/edacourse/api/search/domain/model/SearchResult.java`

**Step 1: Create the value object**

```java
package com.edacourse.api.search.domain.model;

public record SearchResult(
    String productId,
    String name,
    String description,
    double price,
    String category,
    int stock,
    double score
) {}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/search/domain/model/SearchResult.java
git commit -m "feat(search): add SearchResult domain value object"
```

---

### Task 3: Create `ProductSearchRepository` domain interface

**Files:**
- Create: `src/main/java/com/edacourse/api/search/domain/repository/ProductSearchRepository.java`

**Step 1: Create the interface**

```java
package com.edacourse.api.search.domain.repository;

import com.edacourse.api.search.domain.model.SearchableProduct;
import com.edacourse.api.search.domain.model.SearchResult;
import java.util.List;

public interface ProductSearchRepository {
    void index(SearchableProduct product);
    void delete(String productId);
    List<SearchResult> searchByText(String query, int limit);
    List<SearchResult> searchBySemantic(String query, int limit);
    List<SearchResult> searchHybrid(String query, int limit);
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/search/domain/repository/ProductSearchRepository.java
git commit -m "feat(search): add ProductSearchRepository domain interface"
```

---

### Task 4: Create `SearchResultResponse` application DTO

**Files:**
- Create: `src/main/java/com/edacourse/api/search/application/dto/SearchResultResponse.java`

**Step 1: Create the DTO**

Follow the same pattern as `catalog/application/dto/ProductResponse.java` — private constructor, static factory method.

```java
package com.edacourse.api.search.application.dto;

import com.edacourse.api.search.domain.model.SearchResult;

public class SearchResultResponse {
    private final String productId;
    private final String name;
    private final String description;
    private final double price;
    private final String category;
    private final int stock;
    private final double score;

    private SearchResultResponse(SearchResult r) {
        this.productId = r.productId();
        this.name = r.name();
        this.description = r.description();
        this.price = r.price();
        this.category = r.category();
        this.stock = r.stock();
        this.score = r.score();
    }

    public static SearchResultResponse from(SearchResult r) {
        return new SearchResultResponse(r);
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public int getStock() { return stock; }
    public double getScore() { return score; }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/search/application/dto/SearchResultResponse.java
git commit -m "feat(search): add SearchResultResponse application DTO"
```

---

### Task 5: Create `OpenSearchProductSearchRepository` infrastructure implementation

This is the big one. It replaces the current generic `OpenSearchRepository` with a domain-oriented implementation that:
- Owns all OpenSearch query DSL (multi_match, knn, bool/should)
- Owns all JSON construction and response parsing
- Uses `EmbeddingGenerator` internally for semantic queries and indexing
- Implements `ProductSearchRepository`

**Files:**
- Create: `src/main/java/com/edacourse/api/search/infrastructure/opensearch/OpenSearchProductSearchRepository.java`

**Step 1: Create the implementation**

```java
package com.edacourse.api.search.infrastructure.opensearch;

import com.edacourse.api.search.domain.model.SearchableProduct;
import com.edacourse.api.search.domain.model.SearchResult;
import com.edacourse.api.search.domain.repository.ProductSearchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class OpenSearchProductSearchRepository implements ProductSearchRepository {

    private final String baseUrl;
    private final String indexName;
    private final HttpClient httpClient;
    private final EmbeddingGenerator embeddingGenerator;
    private final ObjectMapper objectMapper;

    public OpenSearchProductSearchRepository(String baseUrl, String indexName, EmbeddingGenerator embeddingGenerator) {
        this.baseUrl = baseUrl;
        this.indexName = indexName;
        this.embeddingGenerator = embeddingGenerator;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        createIndexIfNotExists();
    }

    @Override
    public void index(SearchableProduct product) {
        float[] embedding = embeddingGenerator.generate(product.textForEmbedding());

        String doc = """
            {
                "productId": "%s",
                "name": "%s",
                "description": "%s",
                "price": %.2f,
                "category": "%s",
                "stock": %d,
                "embedding": %s,
                "indexed_at": "%s"
            }
            """.formatted(
                escape(product.productId()), escape(product.name()), escape(product.description()),
                product.price(), escape(product.category()), product.stock(),
                embeddingGenerator.toJsonArray(embedding),
                product.indexedAt().toString()
            );

        doPut("/" + indexName + "/_doc/" + product.productId(), doc);
    }

    @Override
    public void delete(String productId) {
        doDelete("/" + indexName + "/_doc/" + productId);
    }

    @Override
    public List<SearchResult> searchByText(String query, int limit) {
        String body = """
            {
                "query": {
                    "multi_match": {
                        "query": "%s",
                        "fields": ["name^3", "description^2", "category"],
                        "fuzziness": "AUTO"
                    }
                },
                "size": %d
            }
            """.formatted(escape(query), limit);
        return parseSearchResponse(doPost("/" + indexName + "/_search", body));
    }

    @Override
    public List<SearchResult> searchBySemantic(String query, int limit) {
        float[] queryEmbedding = embeddingGenerator.generate(query);
        String body = """
            {
                "size": %d,
                "query": {
                    "knn": {
                        "embedding": {
                            "vector": %s,
                            "k": %d
                        }
                    }
                }
            }
            """.formatted(limit, embeddingGenerator.toJsonArray(queryEmbedding), limit);
        return parseSearchResponse(doPost("/" + indexName + "/_search", body));
    }

    @Override
    public List<SearchResult> searchHybrid(String query, int limit) {
        float[] queryEmbedding = embeddingGenerator.generate(query);
        String body = """
            {
                "size": %d,
                "query": {
                    "bool": {
                        "should": [
                            {
                                "multi_match": {
                                    "query": "%s",
                                    "fields": ["name^3", "description^2", "category"],
                                    "fuzziness": "AUTO",
                                    "boost": 1.0
                                }
                            },
                            {
                                "knn": {
                                    "embedding": {
                                        "vector": %s,
                                        "k": %d
                                    }
                                }
                            }
                        ]
                    }
                }
            }
            """.formatted(limit, escape(query), embeddingGenerator.toJsonArray(queryEmbedding), Math.min(limit, 5));
        return parseSearchResponse(doPost("/" + indexName + "/_search", body));
    }

    // --- Index management ---

    private void createIndexIfNotExists() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + indexName))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) return;
        } catch (Exception e) {
            System.err.println("[SEARCH] Error checking index existence: " + e.getMessage());
        }

        String mapping = """
            {
                "settings": {
                    "index": {
                        "knn": true,
                        "number_of_shards": 1,
                        "number_of_replicas": 0
                    },
                    "analysis": {
                        "analyzer": {
                            "spanish_analyzer": {
                                "type": "custom",
                                "tokenizer": "standard",
                                "filter": ["lowercase", "spanish_stop", "spanish_stemmer"]
                            }
                        },
                        "filter": {
                            "spanish_stop": {
                                "type": "stop",
                                "stopwords": "_spanish_"
                            },
                            "spanish_stemmer": {
                                "type": "stemmer",
                                "language": "spanish"
                            }
                        }
                    }
                },
                "mappings": {
                    "properties": {
                        "productId": { "type": "keyword" },
                        "name": {
                            "type": "text",
                            "analyzer": "spanish_analyzer",
                            "fields": { "keyword": { "type": "keyword" } }
                        },
                        "description": { "type": "text", "analyzer": "spanish_analyzer" },
                        "price": { "type": "float" },
                        "category": { "type": "keyword" },
                        "stock": { "type": "integer" },
                        "embedding": {
                            "type": "knn_vector",
                            "dimension": %d,
                            "method": {
                                "name": "hnsw",
                                "space_type": "cosinesimil",
                                "engine": "lucene"
                            }
                        },
                        "indexed_at": { "type": "date" }
                    }
                }
            }
            """.formatted(embeddingGenerator.getDimension());
        String response = doPut("/" + indexName, mapping);
        System.out.println("[SEARCH] Index created: " + response);
    }

    // --- Response parsing ---

    private List<SearchResult> parseSearchResponse(String responseBody) {
        List<SearchResult> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode hits = root.path("hits").path("hits");
            for (JsonNode hit : hits) {
                JsonNode source = hit.path("_source");
                results.add(new SearchResult(
                    source.path("productId").asText(),
                    source.path("name").asText(),
                    source.path("description").asText(),
                    source.path("price").asDouble(),
                    source.path("category").asText(),
                    source.path("stock").asInt(),
                    hit.path("_score").asDouble()
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("[SEARCH] Error parsing OpenSearch response", e);
        }
        return results;
    }

    // --- HTTP helpers (private, not exposed) ---

    private String doPost(String path, String body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("[SEARCH] POST error: " + path, e);
        }
    }

    private String doPut(String path, String body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("[SEARCH] PUT error: " + path, e);
        }
    }

    private String doDelete(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .DELETE()
                .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("[SEARCH] DELETE error: " + path, e);
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/search/infrastructure/opensearch/OpenSearchProductSearchRepository.java
git commit -m "feat(search): add OpenSearchProductSearchRepository with domain-oriented interface"
```

---

### Task 6: Refactor `SearchService` to use domain types

Replace raw JSON string returns with domain types. Remove all OpenSearch query DSL knowledge.

**Files:**
- Modify: `src/main/java/com/edacourse/api/search/application/service/SearchService.java`

**Step 1: Rewrite SearchService**

Replace the entire file contents with:

```java
package com.edacourse.api.search.application.service;

import com.edacourse.api.search.domain.model.SearchableProduct;
import com.edacourse.api.search.domain.model.SearchResult;
import com.edacourse.api.search.domain.repository.ProductSearchRepository;

import java.time.Instant;
import java.util.List;

public class SearchService {

    private final ProductSearchRepository searchRepository;

    public SearchService(ProductSearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public void indexProduct(String productId, String name, String description,
                             double price, String category, int stock, String operation) {
        System.out.println("[SEARCH] " + operation + " -> " + productId +
            " | " + name + " | $" + price + " | stock=" + stock + " | cat=" + category);

        SearchableProduct product = new SearchableProduct(
            productId, name, description, price, category, stock, Instant.now()
        );
        searchRepository.index(product);
    }

    public List<SearchResult> searchFullText(String query) {
        return searchRepository.searchByText(query, 10);
    }

    public List<SearchResult> searchSemantic(String query) {
        return searchRepository.searchBySemantic(query, 10);
    }

    public List<SearchResult> searchHybrid(String query) {
        return searchRepository.searchHybrid(query, 10);
    }
}
```

**Step 2: Verify it compiles**

```bash
cd /Users/antoniosantiagoduenas/Documents/Development/certificatic/proyecto-distribuidos && mvn compile -pl . -q
```

**Step 3: Commit**

```bash
git add src/main/java/com/edacourse/api/search/application/service/SearchService.java
git commit -m "refactor(search): SearchService uses domain types instead of raw JSON"
```

---

### Task 7: Refactor `SearchResource` to return DTOs

**Files:**
- Modify: `src/main/java/com/edacourse/api/search/interfaces/rest/SearchResource.java`

**Step 1: Update SearchResource**

Replace the entire file contents with:

```java
package com.edacourse.api.search.interfaces.rest;

import com.edacourse.api.search.application.dto.SearchResultResponse;
import com.edacourse.api.search.application.service.SearchService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/search")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    private final SearchService searchService;

    @Inject
    public SearchResource(SearchService searchService) {
        this.searchService = searchService;
    }

    @GET
    @Path("full-text")
    public Response searchFullText(@QueryParam("q") String query) {
        List<SearchResultResponse> results = searchService.searchFullText(query)
            .stream().map(SearchResultResponse::from).toList();
        return Response.ok(results).build();
    }

    @GET
    @Path("semantic")
    public Response searchSemantic(@QueryParam("q") String query) {
        List<SearchResultResponse> results = searchService.searchSemantic(query)
            .stream().map(SearchResultResponse::from).toList();
        return Response.ok(results).build();
    }

    @GET
    @Path("hybrid")
    public Response searchHybrid(@QueryParam("q") String query) {
        List<SearchResultResponse> results = searchService.searchHybrid(query)
            .stream().map(SearchResultResponse::from).toList();
        return Response.ok(results).build();
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/edacourse/api/search/interfaces/rest/SearchResource.java
git commit -m "refactor(search): SearchResource returns typed DTOs instead of raw JSON"
```

---

### Task 8: Update `SearchSubscriber` to build `SearchableProduct`

The subscriber currently passes individual fields. No change needed to its signature since `SearchService.indexProduct()` still accepts individual fields and builds the `SearchableProduct` internally. **This task is a no-op** — the subscriber's contract with the service remains the same.

---

### Task 9: Update `Application.java` wiring

Replace the old `SearchRepository`/`OpenSearchRepository` with the new `OpenSearchProductSearchRepository`. Remove the now-unused `searchRepository.createIndexIfNotExist()` call (the new repo does it in its constructor).

**Files:**
- Modify: `src/main/java/com/edacourse/api/Application.java`

**Step 1: Update imports and wiring**

Replace lines 51-56 (old imports):
```java
import com.edacourse.api.search.infrastructure.opensearch.OpenSearchRepository;
import com.edacourse.api.search.infrastructure.opensearch.SearchRepository;
import com.edacourse.api.search.infrastructure.opensearch.EmbeddingGenerator;
import com.edacourse.api.search.infrastructure.opensearch.TrigramEmbeddingGenerator;
```
With:
```java
import com.edacourse.api.search.domain.repository.ProductSearchRepository;
import com.edacourse.api.search.infrastructure.opensearch.OpenSearchProductSearchRepository;
import com.edacourse.api.search.infrastructure.opensearch.EmbeddingGenerator;
import com.edacourse.api.search.infrastructure.opensearch.TrigramEmbeddingGenerator;
```

Replace lines 99-106 (old wiring):
```java
        // OpenSearch
        SearchRepository searchRepository = new OpenSearchRepository();
        EmbeddingGenerator embeddingGenerator = new TrigramEmbeddingGenerator();


        // Search context
        SearchService searchService = new SearchService(searchRepository, embeddingGenerator, "products");
        searchRepository.createIndexIfNotExist("products");
```
With:
```java
        // Search context
        String openSearchUrl = System.getenv().getOrDefault("OPENSEARCH_URL", "http://opensearch:9200");
        EmbeddingGenerator embeddingGenerator = new TrigramEmbeddingGenerator();
        ProductSearchRepository productSearchRepository = new OpenSearchProductSearchRepository(openSearchUrl, "products", embeddingGenerator);
        SearchService searchService = new SearchService(productSearchRepository);
```

**Step 2: Verify it compiles**

```bash
cd /Users/antoniosantiagoduenas/Documents/Development/certificatic/proyecto-distribuidos && mvn compile -pl . -q
```

**Step 3: Commit**

```bash
git add src/main/java/com/edacourse/api/Application.java
git commit -m "refactor(search): wire new OpenSearchProductSearchRepository in Application"
```

---

### Task 10: Delete old files

The old generic `SearchRepository` interface and `OpenSearchRepository` class are no longer used.

**Files:**
- Delete: `src/main/java/com/edacourse/api/search/infrastructure/opensearch/SearchRepository.java`
- Delete: `src/main/java/com/edacourse/api/search/infrastructure/opensearch/OpenSearchRepository.java`

**Step 1: Verify no other references exist**

```bash
grep -r "import.*search.*SearchRepository\b" src/ --include="*.java"
grep -r "import.*search.*OpenSearchRepository\b" src/ --include="*.java"
```

Expected: no matches (only the new `ProductSearchRepository` and `OpenSearchProductSearchRepository` should appear).

**Step 2: Delete**

```bash
rm src/main/java/com/edacourse/api/search/infrastructure/opensearch/SearchRepository.java
rm src/main/java/com/edacourse/api/search/infrastructure/opensearch/OpenSearchRepository.java
```

**Step 3: Verify it compiles**

```bash
cd /Users/antoniosantiagoduenas/Documents/Development/certificatic/proyecto-distribuidos && mvn compile -pl . -q
```

**Step 4: Commit**

```bash
git add -u
git commit -m "refactor(search): remove old generic SearchRepository and OpenSearchRepository"
```

---

### Task 11: Final verification

**Step 1: Full build**

```bash
cd /Users/antoniosantiagoduenas/Documents/Development/certificatic/proyecto-distribuidos && mvn clean compile
```

**Step 2: Verify the new package structure**

```
search/
├── domain/
│   ├── model/
│   │   ├── SearchableProduct.java          ← value object (indexed data)
│   │   └── SearchResult.java               ← value object (query result)
│   └── repository/
│       └── ProductSearchRepository.java    ← domain interface
├── application/
│   ├── dto/
│   │   └── SearchResultResponse.java       ← REST response DTO
│   └── service/
│       └── SearchService.java              ← uses domain types only
├── infrastructure/
│   ├── opensearch/
│   │   ├── OpenSearchProductSearchRepository.java  ← owns all OpenSearch details
│   │   ├── EmbeddingGenerator.java                 ← unchanged
│   │   └── TrigramEmbeddingGenerator.java          ← unchanged
│   └── subscriber/
│       └── SearchSubscriber.java           ← unchanged
└── interfaces/
    └── rest/
        └── SearchResource.java             ← returns List<SearchResultResponse>
```
