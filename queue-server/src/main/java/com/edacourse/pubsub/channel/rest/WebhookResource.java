package com.edacourse.pubsub.channel.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.HeaderParam;
import jakarta.inject.Inject;

import com.edacourse.pubsub.channel.infrastructure.security.HmacSigner;

@Path("/webhook")
public class WebhookResource {

    @Inject
    private HmacSigner hmacSigner;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleWebhook(
        @HeaderParam("X-PubSub-Signature") String sig,
        @HeaderParam("X-PubSub-Channel") String channel,
        String body) {

        String secret = System.getenv("WEBHOOK_SECRET");
        boolean isValid = hmacSigner.verify(body, secret, sig);
        if (!isValid) {
            return Response.status(401).build();
        }
        System.out.println("[RECEIVER] Canal=" + channel
            + " Payload=" + body);
        return Response.ok().build();
    }
}
