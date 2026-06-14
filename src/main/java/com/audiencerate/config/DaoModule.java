package com.audiencerate.config;

import com.audiencerate.dao.DataSourceDao;
import com.audiencerate.dao.DestinationDao;
import com.audiencerate.dao.SegmentDao;
import com.audiencerate.dao.SegmentTrendDao;
import com.audiencerate.dao.ActivationDao;
import com.audiencerate.pool.ActivationsDb;
import com.audiencerate.pool.ProfilesDb;
import com.audiencerate.pool.SegmentsDb;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import javax.sql.DataSource;

public class DaoModule extends AbstractModule {

    @Provides
    @Singleton
    DataSourceDao provideDataSourceDao(@ProfilesDb DataSource ds) {
        return new DataSourceDao(ds);
    }

    @Provides
    @Singleton
    SegmentDao provideSegmentDao(@SegmentsDb DataSource ds) {
        return new SegmentDao(ds);
    }

    @Provides
    @Singleton
    SegmentTrendDao provideSegmentTrendDao(@SegmentsDb DataSource ds) {
        return new SegmentTrendDao(ds);
    }

    @Provides
    @Singleton
    DestinationDao provideDestinationDao(@ActivationsDb DataSource ds) {
        return new DestinationDao(ds);
    }

    @Provides
    @Singleton
    ActivationDao provideActivationDao(@ActivationsDb DataSource ds) {
        return new ActivationDao(ds);
    }
}
