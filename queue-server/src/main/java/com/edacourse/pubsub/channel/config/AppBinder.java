package com.edacourse.pubsub.channel.config;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.edacourse.pubsub.channel.service.ChannelService;
import com.edacourse.pubsub.channel.service.PublishService;
import com.edacourse.pubsub.channel.service.SubscriptionService;

public class AppBinder extends AbstractBinder {
    private final ChannelService channelService;
    private final SubscriptionService subscriptionService;
    private final PublishService publishService;

    public AppBinder(ChannelService channelService, SubscriptionService subscriptionService, PublishService publishService) {
        this.channelService = channelService;
        this.subscriptionService = subscriptionService;
        this.publishService = publishService;
    }

    @Override
    protected void configure() {
        bind(channelService).to(ChannelService.class);
        bind(subscriptionService).to(SubscriptionService.class);
        bind(publishService).to(PublishService.class);
    }
}
