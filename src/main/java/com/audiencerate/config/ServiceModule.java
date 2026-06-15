package com.audiencerate.config;

import com.audiencerate.dao.ActivationDao;
import com.audiencerate.dao.DataSourceDao;
import com.audiencerate.dao.DestinationDao;
import com.audiencerate.dao.SegmentDao;
import com.audiencerate.dao.SegmentTrendDao;
import com.audiencerate.service.ActivationService;
import com.audiencerate.service.DataSourceService;
import com.audiencerate.service.DestinationService;
import com.audiencerate.service.HealthService;
import com.audiencerate.service.OverviewService;
import com.audiencerate.service.SegmentService;
import com.audiencerate.annotations.ActivationsDb;
import com.audiencerate.annotations.ProfilesDb;
import com.audiencerate.annotations.SegmentsDb;
import com.audiencerate.validation.ActivationValidator;
import com.audiencerate.validation.SegmentValidator;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import javax.sql.DataSource;

public class ServiceModule extends AbstractModule {

    @Provides
    @Singleton
    HealthService provideHealthService(
            @ProfilesDb DataSource profilesDs,
            @SegmentsDb DataSource segmentsDs,
            @ActivationsDb DataSource activationsDs) {
        return new HealthService(profilesDs, segmentsDs, activationsDs);
    }

    @Provides
    @Singleton
    OverviewService provideOverviewService(
            DataSourceDao dataSourceDao,
            SegmentDao segmentDao,
            ActivationDao activationDao) {
        return new OverviewService(dataSourceDao, segmentDao, activationDao);
    }

    @Provides
    @Singleton
    DataSourceService provideDataSourceService(DataSourceDao dao) {
        return new DataSourceService(dao);
    }

    @Provides
    @Singleton
    DestinationService provideDestinationService(DestinationDao dao) {
        return new DestinationService(dao);
    }

    @Provides
    @Singleton
    SegmentService provideSegmentService(SegmentDao segmentDao, SegmentTrendDao trendDao,
                                          SegmentValidator validator) {
        return new SegmentService(segmentDao, trendDao, validator);
    }

    @Provides
    @Singleton
    ActivationService provideActivationService(ActivationDao activationDao,
                                                DestinationDao destinationDao,
                                                SegmentDao segmentDao,
                                                ActivationValidator validator,
                                                @ActivationsDb DataSource activationsDs) {
        return new ActivationService(activationDao, destinationDao, segmentDao, validator, activationsDs);
    }

    @Provides
    @Singleton
    SegmentValidator provideSegmentValidator(DataSourceDao dataSourceDao) {
        return new SegmentValidator(dataSourceDao);
    }

    @Provides
    @Singleton
    ActivationValidator provideActivationValidator(SegmentDao segmentDao, DestinationDao destinationDao) {
        return new ActivationValidator(segmentDao, destinationDao);
    }
}
