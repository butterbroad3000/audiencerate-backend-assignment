package com.audiencerate.config;

import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudienceRateModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(AudienceRateModule.class);

    @Override
    protected void configure() {
        log.info("Wiring AudienceRate Guice modules");
        install(new DatabaseModule());
        install(new DaoModule());
        install(new ServiceModule());
    }
}
