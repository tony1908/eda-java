package com.edacourse.pubsub.channel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class SubscriptionResponse {
    private String id;
    private String channelId;
    private String webhookUrl;
    private String description;
    private boolean active;
    private String secret;

    @JsonProperty("created_at")
    private Instant createdAt;

    public SubscriptionResponse(String id, String channelId, String webhookUrl, String description, boolean active, String secret, Instant createdAt) {
        this.id = id;
        this.channelId = channelId;
        this.webhookUrl = webhookUrl;
        this.description = description;
        this.active = active;
        this.secret = secret;
        this.createdAt = createdAt;
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

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
