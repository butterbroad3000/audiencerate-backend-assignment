package com.audiencerate.resource;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Path("/openapi.json")
@Produces(MediaType.APPLICATION_JSON)
public class OpenApiResource {

    private static final Logger log = LoggerFactory.getLogger(OpenApiResource.class);

    private volatile OpenAPI cachedSpec;

    @GET
    public Response spec() {
        try {
            if (cachedSpec == null) {
                synchronized (this) {
                    if (cachedSpec == null) {
                        SwaggerConfiguration config = new SwaggerConfiguration()
                                .resourcePackages(Set.of("com.audiencerate.resource"))
                                .prettyPrint(true)
                                .readAllResources(false);

                        OpenApiContext ctx = new JaxrsOpenApiContextBuilder<>()
                                .openApiConfiguration(config)
                                .buildContext(true);

                        cachedSpec = ctx.read();
                        if (cachedSpec.getInfo() == null) {
                            cachedSpec.setInfo(new Info()
                                    .title("AudienceRate API")
                                    .version("1.0.0")
                                    .description("REST API for AudienceRate DMP"));
                        }

                        // Re-parse to resolve $ref issues
                        cachedSpec = Json.mapper().readValue(
                                Json.mapper().writeValueAsString(cachedSpec), OpenAPI.class);
                    }
                }
            }
            return Response.ok(Json.pretty(cachedSpec)).build();
        } catch (Exception e) {
            log.error("Failed to generate OpenAPI spec", e);
            return Response.serverError()
                    .entity("{\"error\": \"Failed to generate OpenAPI spec\"}")
                    .build();
        }
    }
}
