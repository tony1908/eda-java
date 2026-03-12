package com.edacourse.api.infrastructure.messaging;

public record OrderCanceledEvent(String id, String reason) {
    
}
