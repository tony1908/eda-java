package com.edacourse.pubsub.channel.repository;

import com.edacourse.pubsub.channel.model.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);
    Optional<Subscription> findById(String id);
    List<Subscription> findByChannelId(String channelId);
    void deactivate(String id);
}
