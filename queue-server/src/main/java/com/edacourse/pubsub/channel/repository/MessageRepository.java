package com.edacourse.pubsub.channel.repository;

import com.edacourse.pubsub.channel.model.Message;

import java.util.List;

public interface MessageRepository {
    Message save(Message message);
    List<Message> findByChannelId(String channelId, int limit);
}
