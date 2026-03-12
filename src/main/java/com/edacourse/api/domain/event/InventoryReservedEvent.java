package com.edacourse.api.domain.event;

public record InventoryReservedEvent(String product, int quantity) {}
