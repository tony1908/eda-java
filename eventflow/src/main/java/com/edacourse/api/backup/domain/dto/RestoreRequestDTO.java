package com.edacourse.api.backup.domain.dto;

public class RestoreRequestDTO {
    private String snapshotId;

    public RestoreRequestDTO() {}
    
    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }
}
