package com.edacourse.api.domain.event;

public record PaymentCompletedEvent(String product, double price) {}
