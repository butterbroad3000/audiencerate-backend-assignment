package com.audiencerate.dao;

import com.audiencerate.model.Activation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivationDao {

    private static final Logger log = LoggerFactory.getLogger(ActivationDao.class);
    private final DataSource ds;

    public ActivationDao(DataSource ds) {
        this.ds = ds;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM activations";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            log.error("Failed to count activations", e);
            throw new RuntimeException("Failed to count activations", e);
        }
    }

    public ActivationListResult list(String segmentId, String destinationId, int page, int pageSize) {
        page = Pagination.normalisePage(page);
        pageSize = Pagination.normalisePageSize(pageSize);
        int offset = (page - 1) * pageSize;

        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (segmentId != null && !segmentId.isBlank()) {
            where.append(" AND segment_id = ?");
            params.add(segmentId);
        }
        if (destinationId != null && !destinationId.isBlank()) {
            where.append(" AND destination_id = ?");
            params.add(destinationId);
        }

        // Count
        long total = 0;
        String countSql = "SELECT COUNT(*) FROM activations" + where;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getLong(1);
            }
        } catch (SQLException e) {
            log.error("Failed to count activations", e);
            throw new RuntimeException("Failed to count activations", e);
        }

        // Data
        String sql = "SELECT id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at FROM activations"
                + where + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        params.add(pageSize);
        params.add(offset);

        List<Activation> result = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query activations", e);
            throw new RuntimeException("Failed to query activations", e);
        }

        return new ActivationListResult(result, total);
    }

    public Activation create(Connection conn, String segmentId, String destinationId) throws SQLException {
        String sql = """
            INSERT INTO activations (id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at)
            VALUES (?, ?, ?, 'syncing', 0, now(), now())
            RETURNING id, segment_id, destination_id, status, synced_profiles, created_at, last_sync_at
            """;
        String newId = generateNextId(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newId);
            ps.setString(2, segmentId);
            ps.setString(3, destinationId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRow(rs);
            }
        }
    }

    private String generateNextId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM activations ORDER BY id DESC LIMIT 1";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String lastId = rs.getString(1); // e.g., "act_0019"
                int num = Integer.parseInt(lastId.substring(4)) + 1;
                return String.format("act_%04d", num);
            }
        }
        return "act_0001";
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
