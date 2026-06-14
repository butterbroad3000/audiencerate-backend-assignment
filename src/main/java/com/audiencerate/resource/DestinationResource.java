package com.audiencerate.resource;

import com.audiencerate.model.Destination;
import com.audiencerate.model.response.DataWrapper;
import com.audiencerate.service.DestinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/destinations")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Destinations", description = "Activation destinations from the activations database")
public class DestinationResource {

    private final DestinationService service;

    @Inject
    public DestinationResource(DestinationService service) {
        this.service = service;
    }

    @GET
    @Operation(summary = "List all destinations")
    @ApiResponse(responseCode = "200", description = "List of destinations")
    public Response list() {
        List<Destination> data = service.list();
        return Response.ok(new DataWrapper<>(data)).build();
    }
}
