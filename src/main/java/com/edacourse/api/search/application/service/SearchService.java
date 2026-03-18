package com.edacourse.api.search.application.service;

import com.edacourse.api.search.infrastructure.opensearch.EmbeddingGenerator;
import com.edacourse.api.search.infrastructure.opensearch.SearchRepository;
import java.time.Instant;
import java.util.regex.Pattern;


/**
 * Servicio de busqueda de productos.
 * Por ahora solo registra los cambios detectados.
 * Se integrara con OpenSearch para indexacion y busqueda.
 */
public class SearchService {

    private final SearchRepository searchRepository;
    private final EmbeddingGenerator embeddingGenerator;
    private final String indexName;


    public SearchService(SearchRepository searchRepository, EmbeddingGenerator embeddingGenerator, String indexName) {
        this.searchRepository = searchRepository;
        this.embeddingGenerator = embeddingGenerator;
        this.indexName = indexName;
    }

    public void indexProduct(String productId, String name, String description,
                             double price, String category, int stock, String operation) {
        System.out.println("[SEARCH] " + operation + " -> " + productId +
            " | " + name + " | $" + price + " | stock=" + stock + " | cat=" + category);
        
        String textForEmbedding = name + " " + description + " " + category;
        float[] embedding = embeddingGenerator.generate(textForEmbedding);

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
                escape(productId), escape(name), escape(description),
                price, escape(category), stock,
                embeddingGenerator.toJsonArray(embedding),
                Instant.now().toString()
            );
                       
        

        String response = searchRepository.put("/" + indexName + "/_doc/" + productId, doc);
    }

    public String searchFullText(String query) {
        String body = """
            {
                "query": {
                    "multi_match": {
                        "query": "%s",
                        "fields": ["name^3", "description^2", "category"],
                        "fuzziness": "AUTO"
                    }
                },
                "size": 10
            }
            """.formatted(escape(query));
        return searchRepository.post("/" + indexName + "/_search", body);
    }

    public String searchSemantic(String query) {
        float[] queryEmbedding = embeddingGenerator.generate(query);
        String body = """
            {
                "size": 10,
                "query": {
                    "knn": {
                        "embedding": {
                            "vector": %s,
                            "k": 10
                        }
                    }
                }
            }
            """.formatted(embeddingGenerator.toJsonArray(queryEmbedding));
        return searchRepository.post("/" + indexName + "/_search", body);
    }

    public String searchHybrid(String query) {
        float[] queryEmbedding = embeddingGenerator.generate(query);
        String body = """
            {
                "size": 10,
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
                                        "k": 5
                                    }
                                }
                            }
                        ]
                    }
                }
            }
            """.formatted(escape(query), embeddingGenerator.toJsonArray(queryEmbedding));
        return searchRepository.post("/" + indexName + "/_search", body);
    }



    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
