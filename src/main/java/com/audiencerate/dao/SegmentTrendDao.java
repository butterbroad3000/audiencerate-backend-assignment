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

    private static final Logger LOG = LoggerFactory.getLogger(SegmentTrendDao.class);
    private static final String ERR_QUERY_SEGMENT_TREND = "Failed to query segment_trend";
    private final DataSource ds;

    public SegmentTrendDao(DataSource ds) {
        this.ds = ds;
    }

    public List<SegmentTrendPoint> findBySegmentId(String segmentId, int rangeDays) {
        String sql = SegmentTrendSql.SELECT_BY_SEGMENT_ID;
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
            LOG.error("{} for segment={}", ERR_QUERY_SEGMENT_TREND, segmentId, e);
            throw new RuntimeException(ERR_QUERY_SEGMENT_TREND, e);
        }
        return result;
    }
}
