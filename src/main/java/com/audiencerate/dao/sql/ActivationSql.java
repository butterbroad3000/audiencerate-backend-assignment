package com.audiencerate.dao.sql;

import com.audiencerate.dao.ActivationDao;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL constants and query builders for {@link ActivationDao}.
 */
public final class ActivationSql {

    private ActivationSql() {}

    public static final String COUNT_ALL = "SELECT COUNT(*) FROM activations";

    public static final String SELECT_COLUMNS =
            "SELECT id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at FROM activations";

    public static final String INSERT = """
            INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at)
            VALUES ('act_' || LPAD(nextval('activations_id_seq')::text, 4, '0'), ?, ?, 'syncing', 0, now(), now())
            RETURNING id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at
            """;

    public record ListQuery(String countSql, String dataSql, List<Object> params) {}

    public static ListQuery buildListQuery(String segmentId, String destinationId, int pageSize, int offset) {
        StringBuilder whereCondition = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (segmentId != null && !segmentId.isBlank()) {
            whereCondition.append(" AND segment_id = ?");
            params.add(segmentId);
        }
        if (destinationId != null && !destinationId.isBlank()) {
            whereCondition.append(" AND destination_id = ?");
            params.add(destinationId);
        }

        String countSql = COUNT_ALL + whereCondition;
        String dataSql = SELECT_COLUMNS + whereCondition + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        params.add(pageSize);
        params.add(offset);

        return new ListQuery(countSql, dataSql, params);
    }
}
