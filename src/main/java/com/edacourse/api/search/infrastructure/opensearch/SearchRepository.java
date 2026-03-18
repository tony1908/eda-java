package com.edacourse.api.search.infrastructure.opensearch;

public interface SearchRepository {
    String get(String path);
    String put(String path, String body);
    String post(String path, String body);
    String delete(String path);
    boolean indexExists(String indexName);
    void createIndexIfNotExist(String indexName);
}
