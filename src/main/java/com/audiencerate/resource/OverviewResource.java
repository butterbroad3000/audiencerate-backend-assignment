package com.audiencerate.resource;

import com.audiencerate.model.Overview;
import com.audiencerate.service.OverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/overview")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Overview", description = "Aggregated KPIs across all three databases")
public class OverviewResource {

    private final OverviewService overviewService;

    @Inject
    public OverviewResource(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GET
    @Operation(
            summary = "Get overview KPIs",
            description = "Aggregates totals, status distribution, and top segments from all three databases in a single request.")
    @ApiResponse(responseCode = "200", description = "Overview data")
    public Response overview() {
        Overview overview = overviewService.getOverview();
        return Response.ok(overview).build();
    }
}
