package com.edacourse.pubsub.channel.rest;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.inject.Inject;
import com.edacourse.pubsub.channel.service.ChannelService;
import com.edacourse.pubsub.channel.service.SubscriptionService;
import com.edacourse.pubsub.channel.model.Channel;
import com.edacourse.pubsub.channel.dto.CreateChannelRequest;
import com.edacourse.pubsub.channel.dto.ChannelResponse;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChannelResource {
    @Inject
    private ChannelService channelService;
    
    @Inject
    private SubscriptionService subscriptionService;
    
    @POST
    public Response createChannel(CreateChannelRequest request) {
        System.out.println("Creando canal: " + request.getName());
        try{
            int initialSubscribers = 0;
            Channel channel = channelService.createChannel(request.getName(), request.getDescription());
            ChannelResponse response = toResponse(channel, initialSubscribers);
            return Response.status(Response.Status.CREATED).entity(response).build();
        }catch(Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    public Response listChannels() {
        List<Channel> channels = channelService.listChannels();
        List<ChannelResponse> responses = channels.stream()
            .map(ch -> {
                int subCount = subscriptionService.listSubscribers(ch.getName()).size();
                return toResponse(ch, subCount);
            })
            .toList();
        return Response.ok(responses).build();
    }

    @GET
    @Path("/{name}")
    public Response getChannel(@PathParam("name") String name) {
        return channelService.getChannel(name)
            .map(ch -> {
                int subCount = subscriptionService.listSubscribers(ch.getName()).size();
                return Response.ok(toResponse(ch, subCount)).build();
            })
            .orElse(Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Canal no encontrado: " + name + "\"}")
                .build());
    }

    private ChannelResponse toResponse(Channel channel, int subscriberCount) {
        return new ChannelResponse(channel.getId(), channel.getName(), channel.getDescription(), channel.getCreatedAt(), subscriberCount);
    }
        
}
