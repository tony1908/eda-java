package com.edacourse.api.backup.domain.dto;

public class BackupRequestDTO {
    private String description;

    public BackupRequestDTO() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
