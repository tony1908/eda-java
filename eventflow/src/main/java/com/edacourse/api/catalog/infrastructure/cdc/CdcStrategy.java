package com.edacourse.api.catalog.infrastructure.cdc;

import com.edacourse.api.shared.infrastructure.messaging.EventBus;

public interface CdcStrategy extends AutoCloseable {
    void start(EventBus eventBus, String topic);
    String getName();
    void close();
}
