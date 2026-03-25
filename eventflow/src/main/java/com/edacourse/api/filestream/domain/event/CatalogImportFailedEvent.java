package com.edacourse.api.filestream.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;
import java.time.Instant;

public record CatalogImportFailedEvent(
    String fileId,
    String error,
    Instant failedAt
) implements DomainEvent {}
