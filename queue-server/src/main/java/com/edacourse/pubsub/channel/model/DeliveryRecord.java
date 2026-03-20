package com.edacourse.pubsub.channel.model;

import java.time.Instant;

public class DeliveryRecord {
    private String id;
    private String messageId;
    private String subscriptionId;
    private String channelName;
    private String webhookUrl;
    private String status; // DELIVERED, FAILED, RETRYING
    private int httpStatus;
    private int attempt;
    private String errorMessage;
    private Instant deliveredAt;

    public DeliveryRecord() {}

    public DeliveryRecord(String messageId, String subscriptionId, String channelName,
                          String webhookUrl, String status, int httpStatus, int attempt,
                          String errorMessage) {
        this.messageId = messageId;
        this.subscriptionId = subscriptionId;
        this.channelName = channelName;
        this.webhookUrl = webhookUrl;
        this.status = status;
        this.httpStatus = httpStatus;
        this.attempt = attempt;
        this.errorMessage = errorMessage;
        this.deliveredAt = Instant.now();
    }

    // All getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getHttpStatus() { return httpStatus; }
    public void setHttpStatus(int httpStatus) { this.httpStatus = httpStatus; }
    public int getAttempt() { return attempt; }
    public void setAttempt(int attempt) { this.attempt = attempt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}
