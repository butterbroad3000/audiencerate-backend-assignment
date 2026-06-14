package com.audiencerate.resource;

import com.audiencerate.model.Activation;
import com.audiencerate.model.Segment;
import com.audiencerate.model.SegmentTrendPoint;
import com.audiencerate.model.response.DataWrapper;
import com.audiencerate.model.response.ErrorResponse;
import com.audiencerate.model.response.PagedResponse;
import com.audiencerate.model.request.CreateSegmentRequest;
import com.audiencerate.model.request.UpdateSegmentRequest;
import com.audiencerate.service.ActivationService;
import com.audiencerate.service.SegmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;
import java.util.List;

@Path("/api/segments")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Segments", description = "Audience segments — list, create, update, delete, and related data")
public class SegmentResource {

    private final SegmentService segmentService;
    private final ActivationService activationService;
    private final DataSource segmentsDs;

    @Inject
    public SegmentResource(SegmentService segmentService,
                           ActivationService activationService,
                           @com.audiencerate.pool.SegmentsDb DataSource segmentsDs) {
        this.segmentService = segmentService;
        this.activationService = activationService;
        this.segmentsDs = segmentsDs;
    }

    // ── List ──

    @GET
    @Operation(
            summary = "List segments",
            description = "Paginated list with server-side search, filter, sort, and pagination. Searching matches against name, description, and tags.")
    @ApiResponse(responseCode = "200", description = "Paginated segment list")
    @ApiResponse(responseCode = "400", description = "Invalid query parameters",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response list(
            @Parameter(description = "Page number (1-based)") @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(description = "Items per page (max 100)") @QueryParam("pageSize") @DefaultValue("12") int pageSize,
            @Parameter(description = "Free-text search across name, description, and tags") @QueryParam("search") String search,
            @Parameter(description = "Comma-separated status filter (active,draft,archived)") @QueryParam("status") String status,
            @Parameter(description = "Filter by data source ID") @QueryParam("dataSourceId") String dataSourceId,
            @Parameter(description = "Filter by single tag") @QueryParam("tag") String tag,
            @Parameter(description = "Sort field: name, audienceSize, updatedAt, matchRate (prefix - for desc)") @QueryParam("sort") String sort) {
        PagedResponse<Segment> result = segmentService.list(
                search, status, dataSourceId, tag, sort, page, pageSize);
        return Response.ok(result).build();
    }

    // ── Get by ID ──

    @GET
    @Path("/{id}")
    @Operation(summary = "Get a segment by ID", description = "Returns the segment with its tags and data source IDs.")
    @ApiResponse(responseCode = "200", description = "Segment found")
    @ApiResponse(responseCode = "404", description = "Segment not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response getById(
            @Parameter(description = "Segment ID (e.g. seg_0004)", required = true)
            @PathParam("id") String id) {
        Segment segment = segmentService.getById(id);
        return Response.ok(new DataWrapper<>(segment)).build();
    }

    // ── Trend ──

    @GET
    @Path("/{id}/trend")
    @Operation(summary = "Get segment audience size trend", description = "Daily time series from segment_trend table.")
    @ApiResponse(responseCode = "200", description = "Trend data points")
    @ApiResponse(responseCode = "400", description = "Invalid range parameter",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Segment not found")
    public Response trend(
            @Parameter(description = "Segment ID", required = true) @PathParam("id") String id,
            @Parameter(description = "Number of days to look back (7–180, default 30)") @QueryParam("range") @DefaultValue("30") int range) {
        List<SegmentTrendPoint> trend = segmentService.getTrend(id, range);
        return Response.ok(new DataWrapper<>(trend)).build();
    }

    // ── Activations (cross-context) ──

    @GET
    @Path("/{id}/activations")
    @Operation(
            summary = "Get activations for a segment",
            description = "Cross-context composition: verifies the segment exists (segments DB), fetches its activations (activations DB) with pagination, and enriches each with its destination.")
    @ApiResponse(responseCode = "200", description = "Paginated list of activations with destinations")
    @ApiResponse(responseCode = "404", description = "Segment not found")
    public Response activations(
            @Parameter(description = "Segment ID", required = true) @PathParam("id") String id,
            @Parameter(description = "Page number (1-based)") @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(description = "Items per page (max 100)") @QueryParam("pageSize") @DefaultValue("12") int pageSize) {
        PagedResponse<Activation> result = activationService.getActivationsForSegment(id, page, pageSize);
        return Response.ok(result).build();
    }

    // ── Create ──

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new segment", description = "Name is required (3–80 chars). Inserts segment, tags, and data source links in a single transaction.")
    @ApiResponse(responseCode = "201", description = "Segment created")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response create(
            @Parameter(description = "Segment data", required = true)
            CreateSegmentRequest req) {
        Segment segment = segmentService.create(req, segmentsDs);
        return Response.status(201).entity(new DataWrapper<>(segment)).build();
    }

    // ── Update ──

    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Partial update of a segment", description = "Only provided fields are updated. Null/absent fields are left unchanged.")
    @ApiResponse(responseCode = "200", description = "Segment updated")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Segment not found")
    public Response update(
            @Parameter(description = "Segment ID", required = true) @PathParam("id") String id,
            @Parameter(description = "Fields to update", required = true) UpdateSegmentRequest req) {
        Segment segment = segmentService.update(id, req);
        return Response.ok(new DataWrapper<>(segment)).build();
    }

    // ── Delete ──

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a segment", description = "Cascades to tags, data source links, and trend data.")
    @ApiResponse(responseCode = "204", description = "Segment deleted")
    @ApiResponse(responseCode = "404", description = "Segment not found")
    public Response delete(
            @Parameter(description = "Segment ID", required = true) @PathParam("id") String id) {
        segmentService.delete(id);
        return Response.noContent().build();
    }
}
