package com.audiencerate.resource;

import com.audiencerate.model.DataSource;
import com.audiencerate.model.response.DataWrapper;
import com.audiencerate.service.DataSourceService;
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

import java.util.List;

@Path("/api/data-sources")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Data Sources", description = "Ingestion data sources from the profiles database")
public class DataSourceResource {

    private final DataSourceService service;

    @Inject
    public DataSourceResource(DataSourceService service) {
        this.service = service;
    }

    @GET
    @Operation(summary = "List all data sources")
    @ApiResponse(responseCode = "200", description = "List of data sources")
    public Response list() {
        List<DataSource> data = service.list();
        return Response.ok(new DataWrapper<>(data)).build();
    }
}
