package com.audiencerate.resource;

import com.audiencerate.model.Activation;
import com.audiencerate.model.response.DataWrapper;
import com.audiencerate.model.response.ErrorResponse;
import com.audiencerate.model.response.PagedResponse;
import com.audiencerate.model.request.CreateActivationRequest;
import com.audiencerate.service.ActivationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;

@Path("/api/activations")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Activations", description = "Segment activations to advertising destinations")
public class ActivationResource {

    private final ActivationService service;
    private final DataSource activationsDs;

    @Inject
    public ActivationResource(ActivationService service,
                              @com.audiencerate.pool.ActivationsDb DataSource activationsDs) {
        this.service = service;
        this.activationsDs = activationsDs;
    }

    @GET
    @Operation(summary = "List activations", description = "Paginated list of activations, each enriched with its destination. Optional filters by segmentId and/or destinationId.")
    @ApiResponse(responseCode = "200", description = "Paginated list of activations with destinations")
    public Response list(
            @Parameter(description = "Filter by segment ID") @QueryParam("segmentId") String segmentId,
            @Parameter(description = "Filter by destination ID") @QueryParam("destinationId") String destinationId,
            @Parameter(description = "Page number (1-based)") @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(description = "Items per page (max 100)") @QueryParam("pageSize") @DefaultValue("12") int pageSize) {
        PagedResponse<com.audiencerate.model.Activation> result = service.list(segmentId, destinationId, page, pageSize);
        return Response.ok(result).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Activate a segment to a destination",
            description = "Cross-context: validates the segment exists (segments DB) and the destination exists (activations DB). Creates a new activation with status 'syncing'.")
    @ApiResponse(responseCode = "201", description = "Activation created")
    @ApiResponse(responseCode = "400", description = "Validation failed — segment or destination not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response create(
            @Parameter(description = "Activation data", required = true)
            CreateActivationRequest req) {
        Activation activation = service.create(req, activationsDs);
        return Response.status(Response.Status.CREATED).entity(new DataWrapper<>(activation)).build();
    }
}
