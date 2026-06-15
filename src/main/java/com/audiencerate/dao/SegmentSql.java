package com.audiencerate.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * SQL constants and query builders for {@link SegmentDao}.
 */
public final class SegmentSql {

    private SegmentSql() {}

    // ── Column selection (reused across multiple queries) ──

    static final String SELECT_COLUMNS = """
            SELECT s.id, s.name, s.description, s.status, s.audience_size, s.match_rate,
                   s.created_by, s.created_at, s.updated_at,
                   COALESCE(
                       (SELECT json_agg(st2.tag ORDER BY st2.tag)
                        FROM segment_tags st2 WHERE st2.segment_id = s.id),
                       '[]'::json
                   ) AS tags,
                   COALESCE(
                       (SELECT json_agg(sds2.data_source_id ORDER BY sds2.data_source_id)
                        FROM segment_data_sources sds2 WHERE sds2.segment_id = s.id),
                       '[]'::json
                   ) AS data_source_ids
            FROM segments s
            """;

    // ── Simple queries ──

    static final String COUNT_ALL = "SELECT COUNT(*) FROM segments";

    static final String COUNT_BY_STATUS = "SELECT COUNT(*) FROM segments WHERE status = ?";

    static final String COUNT_GROUP_BY_STATUS =
            "SELECT status, COUNT(*) as cnt FROM segments GROUP BY status ORDER BY status";

    static final String AVG_MATCH_RATE = "SELECT COALESCE(AVG(match_rate), 0) FROM segments";

    static final String DELETE_BY_ID = "DELETE FROM segments WHERE id = ?";

    static final String FIND_BY_ID = SELECT_COLUMNS + " WHERE s.id = ?";

    static final String FIND_TOP_BY_AUDIENCE_SIZE = SELECT_COLUMNS + " ORDER BY s.audience_size DESC LIMIT ?";

    static final String INSERT = """
            INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at)
            VALUES ('seg_' || LPAD(nextval('segments_id_seq')::text, 4, '0'), ?, ?, ?, ?, ?, ?, now(), now())
            RETURNING id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at
            """;

    static final String INSERT_TAG =
            "INSERT INTO segment_tags (segment_id, tag) VALUES (?, ?) ON CONFLICT DO NOTHING";

    static final String INSERT_DATA_SOURCE =
            "INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

    static final String DELETE_TAGS_BY_SEGMENT = "DELETE FROM segment_tags WHERE segment_id = ?";

    static final String DELETE_SOURCES_BY_SEGMENT = "DELETE FROM segment_data_sources WHERE segment_id = ?";

    // ── Allowed sort fields ──

    private static final Set<String> ALLOWED_SORTS = Set.of(
            "name", "-name",
            "audienceSize", "-audienceSize",
            "updatedAt", "-updatedAt",
            "matchRate", "-matchRate");

    // ── Query result records ──

    public record ListQuery(String countSql, String dataSql, List<Object> params) {}

    public record UpdateQuery(String sql, List<Object> params) {}

    // ── Builders ──

    public static ListQuery buildListQuery(String search, String status, String dataSourceId,
                                            String tag, String sort, int pageSize, int offset) {
        String orderClause = buildOrderClause(sort);

        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        boolean joinTags = false;
        boolean joinSources = false;

        if (search != null && !search.isBlank()) {
            joinTags = true;
            where.append(" AND (s.name ILIKE ? OR s.description ILIKE ? OR st.tag ILIKE ?)");
            String pattern = "%%%s%%".formatted(search);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND s.status = ANY(string_to_array(?, ','))");
            params.add(status);
        }
        if (tag != null && !tag.isBlank()) {
            joinTags = true;
            where.append(" AND st.tag = ?");
            params.add(tag);
        }
        if (dataSourceId != null && !dataSourceId.isBlank()) {
            joinSources = true;
            where.append(" AND sds.data_source_id = ?");
            params.add(dataSourceId);
        }

        String joinTagClause = joinTags
                ? " LEFT JOIN segment_tags st ON st.segment_id = s.id" : "";
        String joinSourceClause = joinSources
                ? " LEFT JOIN segment_data_sources sds ON sds.segment_id = s.id" : "";

        String countSql = "SELECT COUNT(DISTINCT s.id) FROM segments s" + joinTagClause + joinSourceClause + where;
        String dataSql = SELECT_COLUMNS + joinTagClause + joinSourceClause + where
                + " GROUP BY s.id" + " " + orderClause + " LIMIT ? OFFSET ?";
        params.add(pageSize);
        params.add(offset);

        return new ListQuery(countSql, dataSql, params);
    }

    public static UpdateQuery buildUpdateQuery(String id, String name, String description, String status) {
        StringBuilder sql = new StringBuilder("UPDATE segments SET updated_at = now()");
        List<Object> params = new ArrayList<>();

        if (name != null) {
            sql.append(", name = ?");
            params.add(name);
        }
        if (description != null) {
            sql.append(", description = ?");
            params.add(description);
        }
        if (status != null) {
            sql.append(", status = ?");
            params.add(status);
        }
        sql.append(" WHERE id = ?");
        params.add(id);

        return new UpdateQuery(sql.toString(), params);
    }

    public static String buildOrderClause(String sort) {
        if (sort == null || !ALLOWED_SORTS.contains(sort)) {
            return "ORDER BY s.updated_at DESC, s.id ASC";
        }
        return switch (sort) {
            case "name"          -> "ORDER BY s.name ASC, s.id ASC";
            case "-name"         -> "ORDER BY s.name DESC, s.id ASC";
            case "audienceSize"  -> "ORDER BY s.audience_size ASC, s.id ASC";
            case "-audienceSize" -> "ORDER BY s.audience_size DESC, s.id ASC";
            case "updatedAt"     -> "ORDER BY s.updated_at ASC, s.id ASC";
            case "-updatedAt"    -> "ORDER BY s.updated_at DESC, s.id ASC";
            case "matchRate"     -> "ORDER BY s.match_rate ASC, s.id ASC";
            case "-matchRate"    -> "ORDER BY s.match_rate DESC, s.id ASC";
            default              -> "ORDER BY s.updated_at DESC, s.id ASC";
        };
    }
}
