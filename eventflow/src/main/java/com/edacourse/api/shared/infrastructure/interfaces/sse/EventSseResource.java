package com.edacourse.api.shared.infrastructure.interfaces.sse;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.ws.rs.core.Context;
import jakarta.inject.Singleton;
import jakarta.inject.Inject;

import com.edacourse.api.shared.infrastructure.sse.EventSseBroadcaster;


@Singleton
@Path("/api/events")
public class EventSseResource {
    private final EventSseBroadcaster broadcaster;

    @Inject
    public EventSseResource(EventSseBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Context
    public void setSse(Sse sse) {
        if (!broadcaster.isReady()) {
            broadcaster.initialize(sse);
        }
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void stream(@Context SseEventSink sink,
                       @HeaderParam("Last-Event-ID") String lastEventId) {
        broadcaster.register(sink, lastEventId);
    }
}
