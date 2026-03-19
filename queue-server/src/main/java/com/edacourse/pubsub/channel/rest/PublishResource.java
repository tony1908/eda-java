package com.edacourse.pubsub.channel.rest;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.inject.Inject;

import com.edacourse.pubsub.channel.service.ChannelService;
import com.edacourse.pubsub.channel.service.PublishService;
import com.edacourse.pubsub.channel.model.Message;
import com.edacourse.pubsub.channel.dto.PublishRequest;
import com.edacourse.pubsub.channel.dto.MessageResponse;

import java.util.List;

@Path("/api/channels/{name}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublishResource {
    @Inject
    private ChannelService channelService;

    @Inject
    private PublishService publishService;

    @POST
    @Path("/publish")
    public Response publish(@PathParam("name") String channelName, PublishRequest request) {
        if (request.getPayload() == null || request.getPayload().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"El payload es requerido\"}")
                .build();
        }

        try {
            Message message = publishService.publish(
                channelName, request.getPayload(), request.getPublisherId());
            MessageResponse response = toResponse(channelName, message);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/history")
    public Response getHistory(@PathParam("name") String channelName,
                               @QueryParam("limit") @DefaultValue("20") int limit) {
        return channelService.getChannel(channelName)
            .map(channel -> {
                List<Message> messages = publishService.getHistory(channelName, limit);
                List<MessageResponse> responses = messages.stream()
                    .map(msg -> toResponse(channelName, msg))
                    .toList();
                return Response.ok(responses).build();
            })
            .orElse(Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Canal no encontrado: " + channelName + "\"}")
                .build());
    }

    private MessageResponse toResponse(String channelName, Message message) {
        return new MessageResponse(
            message.getId(),
            channelName,
            message.getPayload(),
            message.getPublisherId(),
            message.getPublishedAt()
        );
    }
}
