package com.edacourse.api.backup.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;

public record FileBackupRequestedEvent(
    String backupId,
    String fileName,
    String filePath
) implements DomainEvent {}
