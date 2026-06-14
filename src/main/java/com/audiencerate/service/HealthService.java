package com.audiencerate.service;

import com.audiencerate.model.HealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HealthService {

    private static final Logger log = LoggerFactory.getLogger(HealthService.class);
    private final DataSource profilesDs;
    private final DataSource segmentsDs;
    private final DataSource activationsDs;

    public HealthService(DataSource profilesDs, DataSource segmentsDs, DataSource activationsDs) {
        this.profilesDs = profilesDs;
        this.segmentsDs = segmentsDs;
        this.activationsDs = activationsDs;
    }

    public HealthStatus check() {
        HealthStatus.Builder builder = HealthStatus.check();
        checkPool("profiles", profilesDs, builder);
        checkPool("segments", segmentsDs, builder);
        checkPool("activations", activationsDs, builder);
        return builder.build();
    }

    private void checkPool(String name, DataSource ds, HealthStatus.Builder builder) {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             var rs = ps.executeQuery()) {
            rs.next();
            builder.up(name);
        } catch (SQLException e) {
            log.error("Health check failed for {}", name, e);
            builder.down(name);
        }
    }
}
