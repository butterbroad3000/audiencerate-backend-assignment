package com.audiencerate.dao;

import com.audiencerate.model.Destination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DestinationDao {

    private static final Logger LOG = LoggerFactory.getLogger(DestinationDao.class);
    private static final String ERR_QUERY_DESTINATIONS = "Failed to query destinations";
    private final DataSource ds;

    public DestinationDao(DataSource ds) {
        this.ds = ds;
    }

    public List<Destination> findAll() {
        String sql = DestinationSql.SELECT_ALL;
        List<Destination> result = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOG.error(ERR_QUERY_DESTINATIONS, e);
            throw new RuntimeException(ERR_QUERY_DESTINATIONS, e);
        }
        return result;
    }

    public Optional<Destination> findById(String id) {
        String sql = DestinationSql.SELECT_BY_ID;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.error(ERR_QUERY_DESTINATIONS, e);
            throw new RuntimeException(ERR_QUERY_DESTINATIONS, e);
        }
        return Optional.empty();
    }

    private Destination mapRow(ResultSet rs) throws SQLException {
        return new Destination(rs.getString("id"), rs.getString("name"), rs.getString("color"));
    }
}
