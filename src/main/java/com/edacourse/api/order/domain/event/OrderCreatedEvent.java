package com.edacourse.api.order.domain.event;

import com.edacourse.api.shared.domain.event.DomainEvent;
import java.util.List;

public record OrderCreatedEvent(
    String orderId,
    String customerId,
    List<OrderItemData> items,
    double totalAmount
) implements DomainEvent {

    public record OrderItemData(
        String productId,
        String productName,
        double price,
        int quantity
    ) {}
}
