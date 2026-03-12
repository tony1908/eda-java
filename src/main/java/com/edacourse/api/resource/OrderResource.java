package com.edacourse.api.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;

import com.edacourse.api.service.OrderService;
import com.edacourse.api.dto.CreateOrderRequest;
import com.edacourse.api.dto.CancelOrderRequest;
import com.edacourse.api.dto.OrderResponse;
import com.edacourse.api.domain.Order;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
    private final OrderService orderService;

    @Inject
    public OrderResource(OrderService orderService) {
        this.orderService = orderService;
    }

    @POST
    public Response createOrder(CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return Response.status(Response.Status.CREATED).entity(OrderResponse.from(order)).build();
    }

    @PUT
    @Path("/{id}/cancel")
    public Response cancelOrder(@PathParam("id") String id, CancelOrderRequest request) {
        orderService.cancelOrder(id, request.getReason());
        return Response.ok().build();
    }

}
