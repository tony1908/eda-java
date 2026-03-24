package com.edacourse.api.backup.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record BackupFailedEvent(
    String backupId,
    String error
) implements DomainEvent {}
