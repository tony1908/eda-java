package com.edacourse.api.catalog.interfaces.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;

import com.edacourse.api.catalog.application.service.CatalogService;
import com.edacourse.api.catalog.application.dto.CreateProductRequest;
import com.edacourse.api.catalog.application.dto.UpdateProductRequest;
import com.edacourse.api.catalog.application.dto.ProductResponse;
import com.edacourse.api.catalog.domain.model.Product;

import java.util.List;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CatalogResource {
    private final CatalogService catalogService;

    @Inject
    public CatalogResource(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @POST
    public Response createProduct(CreateProductRequest request) {
        Product product = catalogService.createProduct(request);
        return Response.status(Response.Status.CREATED)
            .entity(ProductResponse.from(product))
            .build();
    }

    @PUT
    @Path("/{id}")
    public Response updateProduct(@PathParam("id") String id, UpdateProductRequest request) {
        Product product = catalogService.updateProduct(id, request);
        return Response.ok(ProductResponse.from(product)).build();
    }

    @GET
    public Response listProducts() {
        List<ProductResponse> products = catalogService.listProducts().stream()
            .map(ProductResponse::from)
            .toList();
        return Response.ok(products).build();
    }

    @GET
    @Path("/{id}")
    public Response getProduct(@PathParam("id") String id) {
        Product product = catalogService.getProduct(id);
        return Response.ok(ProductResponse.from(product)).build();
    }
}
