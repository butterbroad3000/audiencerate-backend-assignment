package com.audiencerate.dao;

import com.audiencerate.model.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SegmentDao {

    private static final Logger log = LoggerFactory.getLogger(SegmentDao.class);
    private static final Set<String> ALLOWED_SORTS = Set.of(
            "name", "-name",
            "audienceSize", "-audienceSize",
            "updatedAt", "-updatedAt",
            "matchRate", "-matchRate");

    private final DataSource ds;
    private final ObjectMapper om;

    public SegmentDao(DataSource ds, ObjectMapper om) {
        this.ds = ds;
        this.om = om;
    }

    public SegmentListResult list(String search, String status, String dataSourceId,
                                   String tag, String sort, int page, int pageSize) {
        page = Pagination.normalisePage(page);
        pageSize = Pagination.normalisePageSize(pageSize);
        String orderClause = buildOrderClause(sort);
        int offset = (page - 1) * pageSize;

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        // Build WHERE clause dynamically
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        boolean joinTags = false;
        boolean joinSources = false;

        if (search != null && !search.isBlank()) {
            joinTags = true;
            where.append(" AND (s.name ILIKE ? OR s.description ILIKE ? OR st.tag ILIKE ?)");
            String pattern = "%" + search + "%";
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

        // Total count first
        String countSql = "SELECT COUNT(DISTINCT s.id) FROM segments s"
                + joinTagClause + joinSourceClause + where;
        long total = 0;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getLong(1);
            }
        } catch (SQLException e) {
            log.error("Failed to count segments", e);
            throw new RuntimeException("Failed to count segments", e);
        }

        // Main data query
        sql.append("""
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
            """);
        sql.append(joinTagClause);
        sql.append(joinSourceClause);
        sql.append(where);
        sql.append(" GROUP BY s.id");
        sql.append(" ").append(orderClause);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        List<Segment> segments = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    segments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query segments", e);
            throw new RuntimeException("Failed to query segments", e);
        }

        return new SegmentListResult(segments, total);
    }

    public Optional<Segment> findById(String id) {
        String sql = """
            SELECT s.id, s.name, s.description, s.status, s.audience_size, s.match_rate,
                   s.created_by, s.created_at, s.updated_at,
                   COALESCE(
                       (SELECT json_agg(st.tag ORDER BY st.tag)
                        FROM segment_tags st WHERE st.segment_id = s.id),
                       '[]'::json
                   ) AS tags,
                   COALESCE(
                       (SELECT json_agg(sds.data_source_id ORDER BY sds.data_source_id)
                        FROM segment_data_sources sds WHERE sds.segment_id = s.id),
                       '[]'::json
                   ) AS data_source_ids
            FROM segments s
            WHERE s.id = ?
            """;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find segment id={}", id, e);
            throw new RuntimeException("Failed to find segment", e);
        }
        return Optional.empty();
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM segments";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count segments", e);
        }
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM segments WHERE status = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count segments by status", e);
        }
    }

    public List<StatusCount> countGroupByStatus() {
        String sql = "SELECT status, COUNT(*) as cnt FROM segments GROUP BY status ORDER BY status";
        List<StatusCount> result = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new StatusCount(rs.getString("status"), rs.getInt("cnt")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count segments grouped by status", e);
        }
        return result;
    }

    public BigDecimal averageMatchRate() {
        String sql = "SELECT COALESCE(AVG(match_rate), 0) FROM segments";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to average match_rate", e);
        }
    }

    public List<Segment> findTopByAudienceSize(int limit) {
        String sql = """
            SELECT s.id, s.name, s.description, s.status, s.audience_size, s.match_rate,
                   s.created_by, s.created_at, s.updated_at,
                   COALESCE(
                       (SELECT json_agg(st.tag ORDER BY st.tag)
                        FROM segment_tags st WHERE st.segment_id = s.id),
                       '[]'::json
                   ) AS tags,
                   COALESCE(
                       (SELECT json_agg(sds.data_source_id ORDER BY sds.data_source_id)
                        FROM segment_data_sources sds WHERE sds.segment_id = s.id),
                       '[]'::json
                   ) AS data_source_ids
            FROM segments s
            ORDER BY s.audience_size DESC
            LIMIT ?
            """;
        List<Segment> result = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query top segments", e);
        }
        return result;
    }

    public Segment insert(Connection conn, String id, String name, String description,
                           String status, long audienceSize, BigDecimal matchRate,
                           String createdBy) throws SQLException {
        String sql = """
            INSERT INTO segments (id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, now(), now())
            RETURNING id, name, description, status, audience_size, match_rate, created_by, created_at, updated_at
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setString(4, status);
            ps.setLong(5, audienceSize);
            ps.setBigDecimal(6, matchRate);
            ps.setString(7, createdBy);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRowSimple(rs);
            }
        }
    }

    public void insertTags(Connection conn, String segmentId, List<String> tags) throws SQLException {
        if (tags == null || tags.isEmpty()) return;
        String sql = "INSERT INTO segment_tags (segment_id, tag) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String tag : tags) {
                ps.setString(1, segmentId);
                ps.setString(2, tag);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void insertDataSources(Connection conn, String segmentId, List<String> dataSourceIds) throws SQLException {
        if (dataSourceIds == null || dataSourceIds.isEmpty()) return;
        String sql = "INSERT INTO segment_data_sources (segment_id, data_source_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String dsId : dataSourceIds) {
                ps.setString(1, segmentId);
                ps.setString(2, dsId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Segment create(String id, String name, String description, String status,
                          List<String> tags, List<String> dataSourceIds) {
        return executeInTransaction(conn -> {
            Segment segment = insert(conn, id, name, description, status, 0, BigDecimal.ZERO, "API");
            insertTags(conn, id, tags);
            insertDataSources(conn, id, dataSourceIds);
            return findById(id).orElse(segment);
        });
    }

    public Optional<Segment> update(String id, String name, String description, String status,
                                     List<String> tags, List<String> dataSourceIds) {
        return executeInTransaction(conn -> {
            // Update main segment row
            StringBuilder sql = new StringBuilder("UPDATE segments SET updated_at = now()");
            List<Object> params = new ArrayList<>();

            if (name != null) { sql.append(", name = ?"); params.add(name); }
            if (description != null) { sql.append(", description = ?"); params.add(description); }
            if (status != null) { sql.append(", status = ?"); params.add(status); }
            sql.append(" WHERE id = ?");
            params.add(id);

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                int updated = ps.executeUpdate();
                if (updated == 0) return Optional.empty();
            }

            // Replace tags if provided
            if (tags != null) {
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM segment_tags WHERE segment_id = ?")) {
                    del.setString(1, id);
                    del.executeUpdate();
                }
                insertTags(conn, id, tags);
            }

            // Replace data sources if provided
            if (dataSourceIds != null) {
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM segment_data_sources WHERE segment_id = ?")) {
                    del.setString(1, id);
                    del.executeUpdate();
                }
                insertDataSources(conn, id, dataSourceIds);
            }

            return findById(id); // re-read
        });
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM segments WHERE id = ?"; // cascade deletes tags/sources/trend
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Failed to delete segment id={}", id, e);
            throw new RuntimeException("Failed to delete segment", e);
        }
    }

    private String generateNextId() {
        try (Connection conn = ds.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM segments ORDER BY id DESC LIMIT 1")) {
            if (rs.next()) {
                String lastId = rs.getString(1);
                int num = Integer.parseInt(lastId.substring(4)) + 1;
                return String.format("seg_%04d", num);
            }
            return "seg_0001";
        } catch (SQLException e) {
            log.error("Failed to generate next segment id", e);
            throw new RuntimeException("Failed to generate next segment id", e);
        }
    }

    public String nextId() {
        return generateNextId();
    }

    private String buildOrderClause(String sort) {
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

    private List<String> parseJsonArray(ResultSet rs, String column) throws SQLException {
        String json = rs.getString(column);
        if (json == null || json.equals("[]") || json.equals("null")) {
            return List.of();
        }
        try {
            return om.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Failed to parse JSON array: {}", json, e);
            return List.of();
        }
    }

    private Segment mapRow(ResultSet rs) throws SQLException {
        Segment s = mapRowSimple(rs);
        s.setTags(parseJsonArray(rs, "tags"));
        s.setDataSourceIds(parseJsonArray(rs, "data_source_ids"));
        return s;
    }

    private Segment mapRowSimple(ResultSet rs) throws SQLException {
        Segment s = new Segment();
        s.setId(rs.getString("id"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setStatus(rs.getString("status"));
        s.setAudienceSize(rs.getLong("audience_size"));
        s.setMatchRate(rs.getBigDecimal("match_rate"));
        s.setCreatedBy(rs.getString("created_by"));
        s.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        s.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
        return s;
    }

    private <T> T executeInTransaction(SqlFunction<T> fn) {
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = fn.apply(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            log.error("Transaction failed", e);
            throw new RuntimeException("Transaction failed", e);
        }
    }

    @FunctionalInterface
    private interface SqlFunction<T> {
        T apply(Connection conn) throws SQLException;
    }

    public record SegmentListResult(List<Segment> data, long total) {}
    public record StatusCount(String status, int count) {}
}
