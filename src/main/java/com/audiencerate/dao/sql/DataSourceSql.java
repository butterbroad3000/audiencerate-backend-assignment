package com.audiencerate.dao.sql;

import com.audiencerate.dao.DataSourceDao;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL constants and query builders for {@link DataSourceDao}.
 */
public final class DataSourceSql {

    private DataSourceSql() {}

    public static final String SELECT_ALL =
            "SELECT id, name, type, status, profiles_count, match_rate, last_sync_at FROM data_sources ORDER BY id";

    public static final String SELECT_IDS_IN = "SELECT id FROM data_sources WHERE id IN (%s)";

    public static final String SUM_PROFILES_COUNT = "SELECT COALESCE(SUM(profiles_count), 0) FROM data_sources";

    public static String buildInClausePlaceholders(List<String> ids) {
        return ids.stream().map(id -> "?").collect(Collectors.joining(","));
    }
}
