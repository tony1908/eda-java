package com.edacourse.pubsub.channel.service;

import com.edacourse.pubsub.channel.model.Channel;
import com.edacourse.pubsub.channel.repository.ChannelRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChannelService {
    private final ChannelRepository channelRepository;

    public ChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public Channel createChannel(String name, String description) {
        Optional<Channel> existingChannel = channelRepository.findByName(name);
        if (existingChannel.isPresent()) {
            throw new IllegalArgumentException("Channel already exists");
        }

        Channel channel = new Channel();
        channel.setName(name);
        channel.setDescription(description);
        channel.setId(generateChannelId());

        return channelRepository.save(channel);
    }

    public Optional<Channel> getChannel(String name) {
        return channelRepository.findByName(name);
    }

    public List<Channel> listChannels() {
        return channelRepository.findAll();
    }

    public String generateChannelId() {
        return "ch_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
