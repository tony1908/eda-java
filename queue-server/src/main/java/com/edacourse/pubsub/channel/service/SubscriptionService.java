package com.edacourse.pubsub.channel.service;

import com.edacourse.pubsub.channel.model.Subscription;
import com.edacourse.pubsub.channel.model.Channel;
import com.edacourse.pubsub.channel.repository.SubscriptionRepository;
import com.edacourse.pubsub.channel.repository.ChannelRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final ChannelRepository channelRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, ChannelRepository channelRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.channelRepository = channelRepository;
    }

    public Subscription subscribe(String channelName, String webhookUrl, String description) {
        Channel channel = channelRepository.findByName(channelName).orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        Subscription subscription = new Subscription();
        subscription.setChannelId(channel.getId());
        subscription.setId(generateSubscriptionId());
        subscription.setWebhookUrl(webhookUrl);
        subscription.setSecret(generateSecret());
        subscription.setDescription(description);

        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> listSubscribers(String channelName) {
        Channel channel = channelRepository.findByName(channelName).orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        return subscriptionRepository.findByChannelId(channel.getId());
    }

    public void unsubscribe(String subscriptionId) {
        subscriptionRepository.deactivate(subscriptionId);
    }

    public String generateSubscriptionId() {
        return "sub_" + UUID.randomUUID().toString().substring(0,8);
    }

    public String generateSecret() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
}
