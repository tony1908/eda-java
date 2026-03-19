package com.edacourse.pubsub.channel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class ChannelResponse {
    private String id;
    private String name;
    private String description;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("subscriber_count")
    private int subscriberCount;

    public ChannelResponse(String id, String name, String description, Instant createdAt, int subscriberCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.subscriberCount = subscriberCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(int subscriberCount) {
        this.subscriberCount = subscriberCount;
    }
}


