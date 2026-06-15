package com.audiencerate.dao;

/**
 * SQL constants for {@link SegmentTrendDao}.
 */
public final class SegmentTrendSql {

    private SegmentTrendSql() {}

    static final String SELECT_BY_SEGMENT_ID = """
            SELECT day, audience_size, matched_profiles
            FROM segment_trend
            WHERE segment_id = ?
              AND day >= CURRENT_DATE - ?
            ORDER BY day ASC
            """;
}
