package com.edacourse.pubsub.channel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubscribeRequest {
    @JsonProperty("webhook_url")
    private String webhookUrl;

    private String description;

    public SubscribeRequest() {}

    public SubscribeRequest(String webhookUrl, String description) {
        this.webhookUrl = webhookUrl;
        this.description = description;
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
    
}
