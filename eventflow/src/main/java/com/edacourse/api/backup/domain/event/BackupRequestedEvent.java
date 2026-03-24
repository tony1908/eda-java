package com.edacourse.api.backup.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record BackupRequestedEvent(
    String backupId,
    String description
) implements DomainEvent {}
