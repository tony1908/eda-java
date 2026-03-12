package com.edacourse.api.domain.event;

public record OrderCreatedEvent(String product, double price) {}
