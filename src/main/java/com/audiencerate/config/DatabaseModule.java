package com.audiencerate.config;

import com.audiencerate.annotations.ActivationsDb;
import com.audiencerate.annotations.ProfilesDb;
import com.audiencerate.annotations.SegmentsDb;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class DatabaseModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseModule.class);

    @Provides
    @Singleton
    AppConfig provideAppConfig() {
        AppConfig config = AppConfig.fromEnv();
        LOG.info("Loaded configuration: profiles={}, segments={}, activations={}, port={}",
                config.profilesJdbcUrl(), config.segmentsJdbcUrl(),
                config.activationsJdbcUrl(), config.httpPort());
        return config;
    }

    @Provides
    @Singleton
    @ProfilesDb
    DataSource provideProfilesDataSource(AppConfig config) {
        return createPool("profiles-pool", config.profilesJdbcUrl(), config);
    }

    @Provides
    @Singleton
    @SegmentsDb
    DataSource provideSegmentsDataSource(AppConfig config) {
        return createPool("segments-pool", config.segmentsJdbcUrl(), config);
    }

    @Provides
    @Singleton
    @ActivationsDb
    DataSource provideActivationsDataSource(AppConfig config) {
        return createPool("activations-pool", config.activationsJdbcUrl(), config);
    }

    private HikariDataSource createPool(String poolName, String jdbcUrl, AppConfig config) {
        LOG.info("Creating connection pool '{}' → {}", poolName, jdbcUrl);
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(jdbcUrl);
        hc.setUsername(config.dbUser());
        hc.setPassword(config.dbPassword());
        hc.setMaximumPoolSize(config.poolMaxSize());
        hc.setMinimumIdle(config.poolMinIdle());
        hc.setConnectionTimeout(config.connectionTimeoutMs());
        hc.setIdleTimeout(600_000);       // 10 min
        hc.setMaxLifetime(1_800_000);     // 30 min
        hc.setPoolName(poolName);
        return new HikariDataSource(hc);
    }
}
