package com.edacourse.api.backup.domain.port;

public interface ProductExporter {
    int exportToJson(String filePath) throws Exception;
}
