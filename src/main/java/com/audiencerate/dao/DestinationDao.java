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

public class DestinationDao {

    private static final Logger log = LoggerFactory.getLogger(DestinationDao.class);
    private final DataSource ds;

    public DestinationDao(DataSource ds) {
        this.ds = ds;
    }

    public List<Destination> findAll() {
        String sql = "SELECT id, name, color FROM destinations ORDER BY id";
        List<Destination> result = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to query destinations", e);
            throw new RuntimeException("Failed to query destinations", e);
        }
        return result;
    }

    private Destination mapRow(ResultSet rs) throws SQLException {
        return new Destination(rs.getString("id"), rs.getString("name"), rs.getString("color"));
    }
}
