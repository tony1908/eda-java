package com.edacourse.pubsub.channel.rest;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PathParam;
import jakarta.inject.Inject;

import com.edacourse.pubsub.channel.service.ChannelService;
import com.edacourse.pubsub.channel.service.SubscriptionService;
import com.edacourse.pubsub.channel.model.Subscription;
import com.edacourse.pubsub.channel.dto.SubscribeRequest;
import com.edacourse.pubsub.channel.dto.SubscriptionResponse;

import java.util.List;

@Path("/api/subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubscriptionResource {
    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private ChannelService channelService;

    @POST
    @Path("/channels/{name}")
    public Response subscribe(@PathParam("name") String channelName, SubscribeRequest request) {
        if (request.getWebhookUrl() == null || request.getWebhookUrl().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"webhook_url es requerido\"}")
                .build();
        }

        try {
            Subscription subscription = subscriptionService.subscribe(
                channelName, request.getWebhookUrl(), request.getDescription());
            SubscriptionResponse response = toResponse(channelName, subscription);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/channels/{name}/subscribers")
    public Response listSubscribers(@PathParam("name") String channelName) {
        return channelService.getChannel(channelName)
            .map(channel -> {
                List<Subscription> subs = subscriptionService.listSubscribers(channelName);
                List<SubscriptionResponse> responses = subs.stream()
                    .map(sub -> toResponse(channelName, sub))
                    .toList();
                return Response.ok(responses).build();
            })
            .orElse(Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Canal no encontrado: " + channelName + "\"}")
                .build());
    }

    @DELETE
    @Path("/{id}")
    public Response unsubscribe(@PathParam("id") String subscriptionId) {
        try {
            subscriptionService.unsubscribe(subscriptionId);
            return Response.ok("{\"message\": \"Suscripcion desactivada: " + subscriptionId + "\"}").build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }

    private SubscriptionResponse toResponse(String channelName, Subscription subscription) {
        return new SubscriptionResponse(
            subscription.getId(),
            channelName,
            subscription.getWebhookUrl(),
            subscription.getDescription(),
            subscription.isActive(),
            subscription.getSecret(),
            subscription.getCreatedAt()
        );
    }
}
