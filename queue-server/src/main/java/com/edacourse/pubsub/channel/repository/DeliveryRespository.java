package com.edacourse.pubsub.channel.repository;

import com.edacourse.pubsub.channel.model.DeliveryRecord;
import java.util.List;

public interface DeliveryRespository {
    void save(DeliveryRecord record);
    List<DeliveryRecord> findByChannelName(String channelName, int limit);
    List<DeliveryRecord> findBySubscriptionId(String subscriptionId, int limit);
}
