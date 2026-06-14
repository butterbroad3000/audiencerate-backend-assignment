package com.audiencerate.dao;

import com.audiencerate.model.SegmentTrendPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SegmentTrendDao {

    private static final Logger log = LoggerFactory.getLogger(SegmentTrendDao.class);
    private final DataSource ds;

    public SegmentTrendDao(DataSource ds) {
        this.ds = ds;
    }

    public List<SegmentTrendPoint> findBySegmentId(String segmentId, int rangeDays) {
        String sql = """
            SELECT day, audience_size, matched_profiles
            FROM segment_trend
            WHERE segment_id = ?
              AND day >= CURRENT_DATE - ?
            ORDER BY day ASC
            """;
        List<SegmentTrendPoint> result = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, segmentId);
            ps.setInt(2, rangeDays);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new SegmentTrendPoint(
                            rs.getObject("day", LocalDate.class),
                            rs.getLong("audience_size"),
                            rs.getLong("matched_profiles")));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query segment_trend for segment={}", segmentId, e);
            throw new RuntimeException("Failed to query segment_trend", e);
        }
        return result;
    }
}
