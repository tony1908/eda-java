package com.edacourse.api.backup.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record RestoreFailedEvent(
    String restoreId,
    String error
) implements DomainEvent {}
