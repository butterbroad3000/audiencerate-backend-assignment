package com.audiencerate.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

@Path("/swagger-ui")
public class SwaggerUiResource {

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerUiResource.class);
    private static final String WEBJAR = "META-INF/resources/webjars/swagger-ui/5.17.14/";

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response ui() {
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>AudienceRate API</title>
                    <link rel="stylesheet" href="/swagger-ui/swagger-ui.css">
                </head>
                <body>
                    <div id="swagger-ui"></div>
                    <script src="/swagger-ui/swagger-ui-bundle.js"></script>
                    <script>
                        SwaggerUIBundle({
                            url: '/openapi.json',
                            dom_id: '#swagger-ui',
                            deepLinking: true,
                            presets: [SwaggerUIBundle.presets.apis, SwaggerUIBundle.SwaggerUIStandalonePreset]
                        });
                    </script>
                </body>
                </html>
                """;
        return Response.ok(html).build();
    }

    @GET
    @Path("/{file:.+}")
    public Response staticFile(@PathParam("file") String file) {
        String path = "%s%s".formatted(WEBJAR, file);
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        if (is == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String mediaType = file.endsWith(".css") ? "text/css" : "application/javascript";
        try {
            byte[] bytes = is.readAllBytes();
            is.close();
            return Response.ok(bytes).type(mediaType).build();
        } catch (IOException e) {
            LOG.error("Failed to read swagger-ui file: {}", file, e);
            return Response.serverError().build();
        }
    }
}
