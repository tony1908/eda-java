package com.edacourse.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CancelOrderRequest {
    private final String reason;

    @JsonCreator
    public CancelOrderRequest(
        @JsonProperty("reason") String reason
    ) {
        this.reason = reason;
    }

    public String getReason() { return reason; }
} 
