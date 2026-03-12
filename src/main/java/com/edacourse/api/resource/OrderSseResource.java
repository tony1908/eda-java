package com.edacourse.api.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.core.Context;
import jakarta.inject.Singleton;

@Singleton
@Path("/api/orders/events")
public class OrderSseResource {
    private SseBroadcaster broadcaster;
    private Sse sse;

    @Context
    public void setSse(Sse sse) {
        this.sse = sse;
        this.broadcaster = sse.newBroadcaster();
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void subscribe(@Context SseEventSink sink) {
        if (broadcaster != null) {
            broadcaster.register(sink);
        }
    }

    public void broadcast(String eventName, String data) {
        if (broadcaster != null && sse != null) {
            broadcaster.broadcast(sse.newEventBuilder()
                .name(eventName)
                .data(data)
                .build());
        }
    }
}
