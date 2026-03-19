package com.edacourse.pubsub.channel.service;

import com.edacourse.pubsub.channel.infrastructure.messaging.EventBus;
import com.edacourse.pubsub.channel.model.Channel;
import com.edacourse.pubsub.channel.model.Message;
import com.edacourse.pubsub.channel.repository.ChannelRepository;
import com.edacourse.pubsub.channel.repository.MessageRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PublishService {
    private final EventBus eventBus;
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;

    public PublishService(EventBus eventBus, MessageRepository messageRepository, ChannelRepository channelRepository) {
        this.eventBus = eventBus;
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
    }

    public Message publish(String channelName, String payload, String publisherId) {
        Channel channel = channelRepository.findByName(channelName).orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        Message message = new Message();
        message.setChannelId(channel.getId());
        message.setId(generateMessageId());
        message.setPayload(payload);
        message.setPublisherId(publisherId);
        Message savedMessage = messageRepository.save(message);

        eventBus.publish(channelName, String.valueOf(savedMessage.getId()), payload);

        return savedMessage;
    }

    public List<Message> getHistory(String channelName, int limit) {
        Channel channel = channelRepository.findByName(channelName).orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        return messageRepository.findByChannelId(channel.getId(), limit);
    }

    public String generateMessageId() {
        return "msg_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
