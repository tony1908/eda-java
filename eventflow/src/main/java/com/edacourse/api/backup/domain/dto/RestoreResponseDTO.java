package com.edacourse.api.backup.domain.dto;

public class RestoreResponseDTO {
    private String restoreId;
    private String message;

    public RestoreResponseDTO(String restoreId, String message) {
        this.restoreId = restoreId;
        this.message = message;
    }

    public String getRestoreId() {
        return restoreId;
    }

    public void setRestoreId(String restoreId) {
        this.restoreId = restoreId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
