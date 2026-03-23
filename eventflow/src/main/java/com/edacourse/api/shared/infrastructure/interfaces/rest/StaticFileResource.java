package com.edacourse.api.shared.infrastructure.interfaces.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import jakarta.inject.Singleton;

@Singleton
@Path("/")
public class StaticFileResource {
    
    @GET
    @Path("sse-test")
    @Produces(MediaType.TEXT_HTML)
    public Response sseTestPage() {
        InputStream html = getClass().getClassLoader().getResourceAsStream("static/sse-test.html");
        if (html == null) {
            return Response.status(404).entity("Page not found").build();
        }
        return Response.ok(html).build();
    }
}
