package com.edacourse.pubsub.channel.config;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.edacourse.pubsub.channel.service.ChannelService;
import com.edacourse.pubsub.channel.service.PublishService;
import com.edacourse.pubsub.channel.service.SubscriptionService;
import com.edacourse.pubsub.channel.infrastructure.security.HmacSigner;

public class AppBinder extends AbstractBinder {
    private final ChannelService channelService;
    private final SubscriptionService subscriptionService;
    private final PublishService publishService;
    private final HmacSigner hmacSigner;

    public AppBinder(ChannelService channelService, SubscriptionService subscriptionService, PublishService publishService, HmacSigner hmacSigner) {
        this.channelService = channelService;
        this.subscriptionService = subscriptionService;
        this.publishService = publishService;
        this.hmacSigner = hmacSigner;
    }

    @Override
    protected void configure() {
        bind(channelService).to(ChannelService.class);
        bind(subscriptionService).to(SubscriptionService.class);
        bind(publishService).to(PublishService.class);
        bind(hmacSigner).to(HmacSigner.class);
    }
}
