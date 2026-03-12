package com.edacourse.api.domain.event;

public record OrderCanceledEvent(String id, String reason) {
    
}
