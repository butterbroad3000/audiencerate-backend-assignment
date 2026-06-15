package com.audiencerate.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.audiencerate.model.DataSourceInfo;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataSourceDao {

    private static final Logger log = LoggerFactory.getLogger(DataSourceDao.class);
    private final DataSource ds;

    public DataSourceDao(DataSource ds) {
        this.ds = ds;
    }

    public List<DataSourceInfo> findAll() {
        String sql = "SELECT id, name, type, status, profiles_count, match_rate, last_sync_at FROM data_sources ORDER BY id";
        List<DataSourceInfo> result = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to query data_sources", e);
            throw new RuntimeException("Failed to query data_sources", e);
        }
        return result;
    }

    public Set<String> findExistingIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();
        Set<String> result = new HashSet<>();
        // Build IN clause with correct number of placeholders
        String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
        String sql = "SELECT id FROM data_sources WHERE id IN (" + placeholders + ")";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setString(i + 1, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("id"));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query existing data source IDs", e);
            throw new RuntimeException("Failed to query existing data source IDs", e);
        }
        return result;
    }

    public long sumProfilesCount() {
        String sql = "SELECT COALESCE(SUM(profiles_count), 0) FROM data_sources";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            log.error("Failed to sum profiles_count", e);
            throw new RuntimeException("Failed to sum profiles_count", e);
        }
    }

    private DataSourceInfo mapRow(ResultSet rs) throws SQLException {
        return new DataSourceInfo(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("type"),
                rs.getString("status"),
                rs.getLong("profiles_count"),
                rs.getBigDecimal("match_rate"),
                rs.getObject("last_sync_at", java.time.OffsetDateTime.class));
    }
}
