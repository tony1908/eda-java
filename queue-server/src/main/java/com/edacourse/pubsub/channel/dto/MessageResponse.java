package com.edacourse.pubsub.channel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class MessageResponse {
    private String id;

    @JsonProperty("channel_id")
    private String channelId;
    private String payload;

    @JsonProperty("publisher_id")
    private String publisherId;

    @JsonProperty("published_at")
    private Instant publishedAt;

    public MessageResponse(String id, String channelId, String payload, String publisherId, Instant publishedAt) {
        this.id = id;
        this.channelId = channelId;
        this.payload = payload;
        this.publisherId = publisherId;
        this.publishedAt = publishedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
