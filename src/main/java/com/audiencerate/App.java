package com.audiencerate;

import com.audiencerate.config.AppConfig;
import com.audiencerate.config.AudienceRateModule;
import com.audiencerate.error.GenericExceptionMapper;
import com.audiencerate.error.JerseyNotFoundExceptionMapper;
import com.audiencerate.error.NotFoundExceptionMapper;
import com.audiencerate.error.ValidationExceptionMapper;
import com.audiencerate.annotations.ActivationsDb;
import com.audiencerate.annotations.ProfilesDb;
import com.audiencerate.annotations.SegmentsDb;
import com.audiencerate.resource.ActivationResource;
import com.audiencerate.resource.DataSourceResource;
import com.audiencerate.resource.DestinationResource;
import com.audiencerate.resource.HealthResource;
import com.audiencerate.resource.OpenApiResource;
import com.audiencerate.resource.OverviewResource;
import com.audiencerate.resource.SegmentResource;
import com.audiencerate.resource.SwaggerUiResource;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.zaxxer.hikari.HikariDataSource;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import javax.sql.DataSource;

public class App {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new AudienceRateModule());
        AppConfig config = injector.getInstance(AppConfig.class);

        ResourceConfig rc = new ResourceConfig();

        rc.register(injector.getInstance(HealthResource.class));
        rc.register(injector.getInstance(OverviewResource.class));
        rc.register(injector.getInstance(SegmentResource.class));
        rc.register(injector.getInstance(DataSourceResource.class));
        rc.register(injector.getInstance(DestinationResource.class));
        rc.register(injector.getInstance(ActivationResource.class));
        rc.register(injector.getInstance(OpenApiResource.class));
        rc.register(injector.getInstance(SwaggerUiResource.class));

        rc.register(JacksonFeature.class);
        rc.register(ValidationExceptionMapper.class);
        rc.register(NotFoundExceptionMapper.class);
        rc.register(JerseyNotFoundExceptionMapper.class);
        rc.register(GenericExceptionMapper.class);

        ServletContainer servletContainer = new ServletContainer(rc);
        ServletHolder holder = new ServletHolder(servletContainer);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(holder, "/*");

        Server server = new Server(config.httpPort());
        server.setHandler(context);

        // Graceful shutdown hook — stops Jetty and closes all HikariCP pools
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down...");
            try {
                server.stop();
                LOG.info("Jetty stopped");
            } catch (Exception e) {
                LOG.error("Error stopping Jetty", e);
            }
            closePool(injector, ProfilesDb.class, "profiles");
            closePool(injector, SegmentsDb.class, "segments");
            closePool(injector, ActivationsDb.class, "activations");
            LOG.info("Shutdown complete");
        }, "shutdown-hook"));

        server.start();
        LOG.info("AudienceRate API started on http://localhost:{}", config.httpPort());
        LOG.info("Endpoints: /api/health, /api/overview, /api/segments, /api/data-sources, /api/destinations, /api/activations");
        LOG.info("OpenAPI spec: /openapi.json");

        server.join();
    }

    private static void closePool(Injector injector, Class<? extends Annotation> qualifier,
                                   String name) {
        try {
            DataSource ds = injector.getInstance(Key.get(DataSource.class, qualifier));
            if (ds instanceof HikariDataSource hds) {
                hds.close();
                LOG.info("Closed {} pool", name);
            }
        } catch (Exception e) {
            LOG.error("Error closing {} pool", name, e);
        }
    }
}
