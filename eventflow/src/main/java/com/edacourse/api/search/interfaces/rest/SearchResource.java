package com.edacourse.api.search.interfaces.rest;

import com.edacourse.api.search.application.dto.SearchResultResponse;
import com.edacourse.api.search.application.service.SearchService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/search")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    private final SearchService searchService;

    @Inject
    public SearchResource(SearchService searchService) {
        this.searchService = searchService;
    }

    @GET
    @Path("full-text")
    public Response searchFullText(@QueryParam("q") String query) {
        List<SearchResultResponse> results = searchService.searchFullText(query)
            .stream().map(SearchResultResponse::from).toList();
        return Response.ok(results).build();
    }

    @GET
    @Path("semantic")
    public Response searchSemantic(@QueryParam("q") String query) {
        List<SearchResultResponse> results = searchService.searchSemantic(query)
            .stream().map(SearchResultResponse::from).toList();
        return Response.ok(results).build();
    }

    @GET
    @Path("hybrid")
    public Response searchHybrid(@QueryParam("q") String query) {
        List<SearchResultResponse> results = searchService.searchHybrid(query)
            .stream().map(SearchResultResponse::from).toList();
        return Response.ok(results).build();
    }
}
