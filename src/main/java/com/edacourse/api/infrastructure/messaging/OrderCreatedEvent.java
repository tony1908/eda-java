package com.edacourse.api.infrastructure.messaging;

public record OrderCreatedEvent(String product, double price) {}
