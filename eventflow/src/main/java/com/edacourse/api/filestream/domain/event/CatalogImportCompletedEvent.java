package com.edacourse.api.filestream.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;
import java.time.Instant;

public record CatalogImportCompletedEvent(
    String fileId,
    String fileName,
    int productsImported,
    long durationMs,
    Instant completedAt
) implements DomainEvent {}
