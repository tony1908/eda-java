package com.edacourse.api.infrastructure.messaging;

public record OrderEvent(String product, double price) {}
