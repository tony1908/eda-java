package com.edacourse.pubsub.channel.model;

import java.time.Instant;

public class Subscription {
    private String id;
    private String channelId;
    private String webhookUrl;
    private String secret;
    private String description;
    private boolean active;
    private Instant createdAt;

    public Subscription() {}

    public Subscription(String id, String channelId, String webhookUrl, String secret, String description, boolean active, Instant createdAt) {
        this.id = id;
        this.channelId = channelId;
        this.webhookUrl = webhookUrl;
        this.secret = secret;
        this.description = description;
        this.active = active;
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

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
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
}
