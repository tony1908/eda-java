package com.edacourse.pubsub.channel.repository;

import com.edacourse.pubsub.channel.model.Channel;
import java.util.List;
import java.util.Optional;

public interface ChannelRepository {
    Channel save(Channel channel);
    Optional<Channel> findById(String id);
    Optional<Channel> findByName(String name);
    List<Channel> findAll();
    void delete(String id);
}
