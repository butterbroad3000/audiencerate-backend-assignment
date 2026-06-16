package com.audiencerate.dao;

import com.audiencerate.dao.sql.SegmentSql;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SegmentDao {

    private static final Logger LOG = LoggerFactory.getLogger(SegmentDao.class);
    private static final String ERR_COUNT_SEGMENTS = "Failed to count segments";
    private static final String ERR_QUERY_SEGMENTS = "Failed to query segments";
    private static final String ERR_FIND_SEGMENT = "Failed to find segment";
    private static final String ERR_DELETE_SEGMENT = "Failed to delete segment";
    private static final String ERR_TRANSACTION_FAILED = "Transaction failed";

    private final DataSource ds;
    private final ObjectMapper om;

    public SegmentDao(DataSource ds, ObjectMapper om) {
        this.ds = ds;
        this.om = om;
    }

    // ── Public API ──

    public SegmentListResult list(String search, String status, String dataSourceId,
                                   String tag, String sort, int page, int pageSize) {
        int normalisedPage = PaginationUtils.normalisePage(page);
        int normalisedPageSize = PaginationUtils.normalisePageSize(pageSize);
        int offset = (normalisedPage - 1) * normalisedPageSize;

        SegmentSql.ListQuery query = SegmentSql.buildListQuery(search, status, dataSourceId, tag, sort,
                normalisedPageSize, offset);

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try {
                final var total = executeCountQuery(conn, query);
                final var segments = executeListDataQuery(conn, query);
                conn.commit();
                return new SegmentListResult(segments, total);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOG.error(ERR_COUNT_SEGMENTS, e);
            throw new RuntimeException(ERR_COUNT_SEGMENTS, e);
        }
    }

    public Optional<Segment> findById(String id) {
        String sql = SegmentSql.FIND_BY_ID;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.error("{} id={}", ERR_FIND_SEGMENT, id, e);
            throw new RuntimeException(ERR_FIND_SEGMENT, e);
        }
        return Optional.empty();
    }

    public Segment create(String name, String description, String status,
                          List<String> tags, List<String> dataSourceIds) {
        return executeInTransaction(conn -> {
            // audienceSize and matchRate start at 0 — the segment has just been created
            Segment segment = insert(conn, name, description, status, 0, BigDecimal.ZERO, "API");
            String segmentId = segment.id();
            insertTags(conn, segmentId, tags);
            insertDataSources(conn, segmentId, dataSourceIds);
            return segment;
        });
    }

    public Optional<Segment> update(String id, String name, String description, String status,
                                     List<String> tags, List<String> dataSourceIds) {
        return executeInTransaction(conn -> {
            SegmentSql.UpdateQuery updateQuery = SegmentSql.buildUpdateQuery(id, name, description, status);
            try (PreparedStatement ps = conn.prepareStatement(updateQuery.sql())) {
                for (int i = 0; i < updateQuery.params().size(); i++) {
                    ps.setObject(i + 1, updateQuery.params().get(i));
                }
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    return Optional.empty();
                }
            }

            if (tags != null) {
                try (PreparedStatement del = conn.prepareStatement(SegmentSql.DELETE_TAGS_BY_SEGMENT)) {
                    del.setString(1, id);
                    del.executeUpdate();
                }
                insertTags(conn, id, tags);
            }

            if (dataSourceIds != null) {
                try (PreparedStatement del = conn.prepareStatement(SegmentSql.DELETE_SOURCES_BY_SEGMENT)) {
                    del.setString(1, id);
                    del.executeUpdate();
                }
                insertDataSources(conn, id, dataSourceIds);
            }

            return findById(id);
        });
    }

    public boolean delete(String id) {
        String sql = SegmentSql.DELETE_BY_ID;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.error("{} id={}", ERR_DELETE_SEGMENT, id, e);
            throw new RuntimeException(ERR_DELETE_SEGMENT, e);
        }
    }

    public int count() {
        String sql = SegmentSql.COUNT_ALL;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(ERR_COUNT_SEGMENTS, e);
        }
    }

    public int countByStatus(String status) {
        String sql = SegmentSql.COUNT_BY_STATUS;
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
        String sql = SegmentSql.COUNT_GROUP_BY_STATUS;
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
        String sql = SegmentSql.AVG_MATCH_RATE;
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
        String sql = SegmentSql.FIND_TOP_BY_AUDIENCE_SIZE;
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

    // ── Package-private helpers used by create / update ──

    Segment insert(Connection conn, String name, String description,
                   String status, long audienceSize, BigDecimal matchRate,
                   String createdBy) throws SQLException {
        String sql = SegmentSql.INSERT;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, status);
            ps.setLong(4, audienceSize);
            ps.setBigDecimal(5, matchRate);
            ps.setString(6, createdBy);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRow(rs);
            }
        }
    }

    void insertTags(Connection conn, String segmentId, List<String> tags) throws SQLException {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        String sql = SegmentSql.INSERT_TAG;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String tag : tags) {
                ps.setString(1, segmentId);
                ps.setString(2, tag);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    void insertDataSources(Connection conn, String segmentId, List<String> dataSourceIds) throws SQLException {
        if (dataSourceIds == null || dataSourceIds.isEmpty()) {
            return;
        }
        String sql = SegmentSql.INSERT_DATA_SOURCE;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String dsId : dataSourceIds) {
                ps.setString(1, segmentId);
                ps.setString(2, dsId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ── Private helpers ──

    private long executeCountQuery(Connection conn, SegmentSql.ListQuery query) {
        try (PreparedStatement ps = conn.prepareStatement(query.countSql())) {
            int filterParamCount = query.params().size() - 2;
            for (int i = 0; i < filterParamCount; i++) {
                ps.setObject(i + 1, query.params().get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            LOG.error(ERR_COUNT_SEGMENTS, e);
            throw new RuntimeException(ERR_COUNT_SEGMENTS, e);
        }
        return 0;
    }

    private List<Segment> executeListDataQuery(Connection conn, SegmentSql.ListQuery query) {
        List<Segment> segments = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(query.dataSql())) {
            for (int i = 0; i < query.params().size(); i++) {
                ps.setObject(i + 1, query.params().get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    segments.add(mapRow(rs));
                }
            }
            return segments;
        } catch (SQLException e) {
            LOG.error(ERR_QUERY_SEGMENTS, e);
            throw new RuntimeException(ERR_QUERY_SEGMENTS, e);
        }
    }

    private List<String> parseJsonArray(ResultSet rs, String column) throws SQLException {
        String json = rs.getString(column);
        if (json == null || json.equals("[]") || json.equals("null")) {
            return List.of();
        }
        try {
            return om.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            LOG.error("Failed to parse JSON array: {}", json, e);
            return List.of();
        }
    }

    private Segment mapRow(ResultSet rs) throws SQLException {
        return new Segment(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getLong("audience_size"),
                rs.getBigDecimal("match_rate"),
                parseJsonArray(rs, "tags"),
                parseJsonArray(rs, "data_source_ids"),
                rs.getString("created_by"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class));
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
            LOG.error(ERR_TRANSACTION_FAILED, e);
            throw new RuntimeException(ERR_TRANSACTION_FAILED, e);
        }
    }

    @FunctionalInterface
    private interface SqlFunction<T> {
        T apply(Connection conn) throws SQLException;
    }

    public record SegmentListResult(List<Segment> data, long total) {}
    public record StatusCount(String status, int count) {}
}
