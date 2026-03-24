package com.edacourse.api.backup.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record RestoreRequestedEvent(
    String restoreId,
    String snapshotId
) implements DomainEvent {}
