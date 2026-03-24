package com.edacourse.api.backup.infrastructure.subscriber;

import com.edacourse.api.backup.application.BackupService;
import com.edacourse.api.shared.infrastructure.messaging.EventBus;
import com.edacourse.api.backup.domain.event.BackupRequestedEvent;
import com.edacourse.api.backup.domain.event.RestoreRequestedEvent;

public class BackupSubscriber {
    private final BackupService backupService;

    public BackupSubscriber(EventBus eventBus, BackupService backupService) {
        this.backupService = backupService;
        eventBus.subscribe("backup.requested", BackupRequestedEvent.class, this::onBackupRequested, "backup-service");
        eventBus.subscribe("restore.requested", RestoreRequestedEvent.class, this::onRestoreRequested, "backup-service");
    }

    private void onBackupRequested(BackupRequestedEvent event) {
        backupService.executeBackup(event.backupId(), event.description());
    }

    private void onRestoreRequested(RestoreRequestedEvent event) {
        backupService.executeRestore(event.restoreId(), event.snapshotId());
    }
}
