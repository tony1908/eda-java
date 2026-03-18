package com.edacourse.api.search.infrastructure.opensearch;

public interface EmbeddingGenerator {
    float[] generate(String text);
    int getDimension();
    String toJsonArray(float[] embedding);
}
