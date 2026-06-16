package com.audiencerate.dao;

import com.audiencerate.dao.sql.ActivationSql;
import com.audiencerate.model.Activation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivationDao {

    private static final Logger LOG = LoggerFactory.getLogger(ActivationDao.class);
    private static final String ERR_COUNT_ACTIVATIONS = "Failed to count activations";
    private static final String ERR_QUERY_ACTIVATIONS = "Failed to query activations";
    private final DataSource ds;

    public ActivationDao(DataSource ds) {
        this.ds = ds;
    }

    public int count() {
        String sql = ActivationSql.COUNT_ALL;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            LOG.error(ERR_COUNT_ACTIVATIONS, e);
            throw new RuntimeException(ERR_COUNT_ACTIVATIONS, e);
        }
    }

    public ActivationListResult list(String segmentId, String destinationId, int page, int pageSize) {
        int normalisedPage = PaginationUtils.normalisePage(page);
        int normalisedPageSize = PaginationUtils.normalisePageSize(pageSize);
        int offset = (normalisedPage - 1) * normalisedPageSize;

        ActivationSql.ListQuery query = ActivationSql.buildListQuery(segmentId, destinationId, normalisedPageSize, offset);
        List<Object> params = query.params();

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try {
                final var total = executeCountQuery(conn, query, params);
                final var result = executeListDataQuery(conn, query, params);
                conn.commit();
                return new ActivationListResult(result, total);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOG.error(ERR_COUNT_ACTIVATIONS, e);
            throw new RuntimeException(ERR_COUNT_ACTIVATIONS, e);
        }
    }

    private long executeCountQuery(Connection conn, ActivationSql.ListQuery query, List<Object> params) {
        try (PreparedStatement ps = conn.prepareStatement(query.countSql())) {
            for (int i = 0; i < params.size() - 2; i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            LOG.error(ERR_COUNT_ACTIVATIONS, e);
            throw new RuntimeException(ERR_COUNT_ACTIVATIONS, e);
        }
    }

    private List<Activation> executeListDataQuery(Connection conn, ActivationSql.ListQuery query, List<Object> params) {
        List<Activation> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(query.dataSql())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            LOG.error(ERR_QUERY_ACTIVATIONS, e);
            throw new RuntimeException(ERR_QUERY_ACTIVATIONS, e);
        }
    }

    public Activation create(Connection conn, String segmentId, String destinationId) throws SQLException {
        String sql = ActivationSql.INSERT;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, segmentId);
            ps.setString(2, destinationId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRow(rs);
            }
        }
    }

    public record ActivationListResult(List<Activation> data, long total) {}

    private Activation mapRow(ResultSet rs) throws SQLException {
        Activation a = new Activation();
        a.setId(rs.getString("id"));
        a.setSegmentId(rs.getString("segment_id"));
        a.setDestinationId(rs.getString("destination_id"));
        a.setStatus(rs.getString("status"));
        a.setSyncedProfiles(rs.getLong("synced_profiles"));
        a.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        a.setLastSyncAt(rs.getObject("last_sync_at", OffsetDateTime.class));
        return a;
    }
}
