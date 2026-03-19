package com.edacourse.api.search.infrastructure.opensearch;

public class TrigramEmbeddingGenerator implements EmbeddingGenerator {
    private static final int VECTOR_DIMENSION = 128;   
    
    @Override
    public float[] generate(String text) {
        if (text == null || text.isBlank()) {
            return new float[VECTOR_DIMENSION];
        }

        float[] vector = new float[VECTOR_DIMENSION];
        String normalized = text.toLowerCase().trim();
        
        for (int i = 0; i < normalized.length() - 2; i++) {
            String trigram = normalized.substring(i, i + 3);
            int hash = Math.abs(trigram.hashCode());
            int index = hash % VECTOR_DIMENSION;
            vector[index] += 1.0f;
        }
        
        normalize(vector);
        return vector;
    }

    private void normalize(float[] vector) {
        float sum = 0;
        for (float v : vector) {
            sum += v * v;
        }
        float magnitude = (float) Math.sqrt(sum);
        if (magnitude > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= magnitude;
            }
        }
    }

    @Override
    public int getDimension() {
        return VECTOR_DIMENSION;
    }

    @Override
    public String toJsonArray(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
