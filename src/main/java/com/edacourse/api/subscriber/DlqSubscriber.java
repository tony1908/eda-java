package com.edacourse.api.subscriber;

import com.edacourse.api.infrastructure.messaging.DeadLetterHandler;
import com.edacourse.api.infrastructure.messaging.EventHandler;
import com.edacourse.api.domain.event.OrderCreatedEvent;

public class DlqSubscriber {

    public DlqSubscriber(DeadLetterHandler deadLetterHandler) {
        deadLetterHandler.onDeadLetter("orders.created", OrderCreatedEvent.class, this::onDeadLetter);
    }
    
    public void onDeadLetter(OrderCreatedEvent event) {
        System.err.println("Mensaje perdido: " + event);
    }
}
