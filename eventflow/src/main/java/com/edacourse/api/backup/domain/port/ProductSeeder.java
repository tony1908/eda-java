package com.edacourse.api.backup.domain.port;

public interface ProductSeeder {
    SeedResult seed(int count);

    record SeedResult(int inserted, long elapsedMs) {}
}
