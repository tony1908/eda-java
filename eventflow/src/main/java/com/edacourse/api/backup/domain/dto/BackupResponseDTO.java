package com.edacourse.api.backup.domain.dto;

public class BackupResponseDTO {
    private String backupId;
    private String message;

    public BackupResponseDTO(String backupId, String message) {
        this.backupId = backupId;
        this.message = message;
    }

    public String getBackupId() {
        return backupId;
    }

    public void setBackupId(String backupId) {
        this.backupId = backupId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
