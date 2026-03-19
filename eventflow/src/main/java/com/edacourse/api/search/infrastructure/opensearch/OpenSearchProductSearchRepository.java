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

    // --- HTTP helpers (private) ---

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
