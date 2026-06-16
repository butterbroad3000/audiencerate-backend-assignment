package com.audiencerate.dao.sql;

import com.audiencerate.dao.SegmentTrendDao;

/**
 * SQL constants for {@link SegmentTrendDao}.
 */
public final class SegmentTrendSql {

    private SegmentTrendSql() {}

    public static final String SELECT_BY_SEGMENT_ID = """
            SELECT day, audience_size, matched_profiles
            FROM segment_trend
            WHERE segment_id = ?
              AND day >= CURRENT_DATE - ?
            ORDER BY day ASC
            """;
}
