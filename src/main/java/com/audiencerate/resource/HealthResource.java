package com.audiencerate.resource;

import com.audiencerate.model.HealthStatus;
import com.audiencerate.service.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Health", description = "Liveness and readiness checks")
public class HealthResource {

    private final HealthService healthService;

    @Inject
    public HealthResource(HealthService healthService) {
        this.healthService = healthService;
    }

    @GET
    @Operation(
            summary = "Health check",
            description = "Verifies each of the three database pools can serve a connection.")
    @ApiResponse(responseCode = "200", description = "All databases up",
            content = @Content(schema = @Schema(implementation = HealthStatus.class)))
    @ApiResponse(responseCode = "503", description = "One or more databases down")
    public Response health() {
        HealthStatus status = healthService.check();
        int httpStatus = "ok".equals(status.status()) ? 200 : 503;
        return Response.status(httpStatus).entity(status).build();
    }
}
