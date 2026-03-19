package com.edacourse.pubsub.channel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PublishRequest {
    private String payload;

    @JsonProperty("publisher_id")
    private String publisherId;

    public PublishRequest() {}

    public PublishRequest(String payload, String publisherId) {
        this.payload = payload;
        this.publisherId = publisherId;
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
}
