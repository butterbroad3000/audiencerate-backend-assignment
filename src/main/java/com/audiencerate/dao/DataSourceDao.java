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
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataSourceDao {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceDao.class);
    private static final String ERR_QUERY_DATA_SOURCES = "Failed to query data_sources";
    private static final String ERR_QUERY_EXISTING_DS_IDS = "Failed to query existing data source IDs";
    private static final String ERR_SUM_PROFILES_COUNT = "Failed to sum profiles_count";
    private final DataSource ds;

    public DataSourceDao(DataSource ds) {
        this.ds = ds;
    }

    public List<DataSourceInfo> findAll() {
        String sql = DataSourceSql.SELECT_ALL;
        List<DataSourceInfo> result = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOG.error(ERR_QUERY_DATA_SOURCES, e);
            throw new RuntimeException(ERR_QUERY_DATA_SOURCES, e);
        }
        return result;
    }

    public Set<String> findExistingIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        Set<String> result = new HashSet<>();
        // Build IN clause with correct number of placeholders
        String placeholders = DataSourceSql.buildInClausePlaceholders(ids);
        String sql = DataSourceSql.SELECT_IDS_IN.formatted(placeholders);
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
            LOG.error(ERR_QUERY_EXISTING_DS_IDS, e);
            throw new RuntimeException(ERR_QUERY_EXISTING_DS_IDS, e);
        }
        return result;
    }

    public long sumProfilesCount() {
        String sql = DataSourceSql.SUM_PROFILES_COUNT;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            LOG.error(ERR_SUM_PROFILES_COUNT, e);
            throw new RuntimeException(ERR_SUM_PROFILES_COUNT, e);
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
                rs.getObject("last_sync_at", OffsetDateTime.class));
    }
}
