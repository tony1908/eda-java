package com.edacourse.api.filestream.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;
import java.time.Instant;

public record FileUploadStartedEvent(
    String fileId,
    String fileName,
    int totalParts,
    long totalBytes,
    Instant startedAt
) implements DomainEvent {}
