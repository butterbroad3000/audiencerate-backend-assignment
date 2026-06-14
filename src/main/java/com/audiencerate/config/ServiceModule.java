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
import com.audiencerate.validation.SegmentValidator;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ServiceModule extends AbstractModule {

    @Provides
    @Singleton
    HealthService provideHealthService(
            @com.audiencerate.pool.ProfilesDb javax.sql.DataSource profilesDs,
            @com.audiencerate.pool.SegmentsDb javax.sql.DataSource segmentsDs,
            @com.audiencerate.pool.ActivationsDb javax.sql.DataSource activationsDs) {
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
                                          SegmentValidator validator, DataSourceDao dataSourceDao) {
        return new SegmentService(segmentDao, trendDao, validator, dataSourceDao);
    }

    @Provides
    @Singleton
    ActivationService provideActivationService(ActivationDao activationDao,
                                                DestinationDao destinationDao,
                                                SegmentDao segmentDao) {
        return new ActivationService(activationDao, destinationDao, segmentDao);
    }

    @Provides
    @Singleton
    SegmentValidator provideSegmentValidator() {
        return new SegmentValidator();
    }
}
