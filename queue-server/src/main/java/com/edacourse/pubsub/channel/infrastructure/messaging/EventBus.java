package com.edacourse.pubsub.channel.infrastructure.messaging;

public interface EventBus {
    void publish(String channelName, String messageId, String payload);
    void close();
}
