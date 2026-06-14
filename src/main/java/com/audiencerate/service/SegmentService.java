package com.audiencerate.service;

import com.audiencerate.dao.DataSourceDao;
import com.audiencerate.dao.SegmentDao;
import com.audiencerate.dao.SegmentDao.SegmentListResult;
import com.audiencerate.dao.SegmentTrendDao;
import com.audiencerate.error.NotFoundException;
import com.audiencerate.error.ValidationException;
import com.audiencerate.model.Segment;
import com.audiencerate.model.SegmentTrendPoint;
import com.audiencerate.model.request.CreateSegmentRequest;
import com.audiencerate.model.request.UpdateSegmentRequest;
import com.audiencerate.model.response.PagedResponse;
import com.audiencerate.model.response.PaginationMeta;
import com.audiencerate.validation.SegmentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SegmentService {

    private static final Logger log = LoggerFactory.getLogger(SegmentService.class);
    private final SegmentDao segmentDao;
    private final SegmentTrendDao trendDao;
    private final SegmentValidator validator;
    private final DataSourceDao dataSourceDao;

    public SegmentService(SegmentDao segmentDao, SegmentTrendDao trendDao, SegmentValidator validator,
                          DataSourceDao dataSourceDao) {
        this.segmentDao = segmentDao;
        this.trendDao = trendDao;
        this.validator = validator;
        this.dataSourceDao = dataSourceDao;
    }

    public PagedResponse<Segment> list(String search, String status, String dataSourceId,
                                        String tag, String sort, int page, int pageSize) {
        SegmentListResult result = segmentDao.list(search, status, dataSourceId, tag, sort, page, pageSize);
        int totalPages = (int) Math.ceil((double) result.total() / pageSize);
        return new PagedResponse<>(
                result.data(),
                new PaginationMeta(page, pageSize, result.total(), totalPages));
    }

    public Segment getById(String id) {
        return segmentDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Segment not found: " + id));
    }

    public List<SegmentTrendPoint> getTrend(String id, int rangeDays) {
        // Verify segment exists
        if (segmentDao.findById(id).isEmpty()) {
            throw new NotFoundException("Segment not found: " + id);
        }
        // Validate range
        if (rangeDays < 7 || rangeDays > 180) {
            throw new ValidationException("Validation failed",
                    Map.of("range", "Range must be between 7 and 180 days"));
        }
        return trendDao.findBySegmentId(id, rangeDays);
    }

    public Segment update(String id, UpdateSegmentRequest req) {
        validator.validateUpdate(req);
        return segmentDao.update(id, req.name() != null ? req.name().trim() : null,
                        req.description(), req.status(),
                        req.tags(), req.dataSourceIds())
                .orElseThrow(() -> new NotFoundException("Segment not found: " + id));
    }

    public void delete(String id) {
        if (!segmentDao.delete(id)) {
            throw new NotFoundException("Segment not found: " + id);
        }
    }

    public Segment create(CreateSegmentRequest req, DataSource ds) {
        validator.validateCreate(req);

        // Validate dataSourceIds exist in profiles DB (cross-database referential integrity)
        if (req.dataSourceIds() != null && !req.dataSourceIds().isEmpty()) {
            Set<String> existingIds = dataSourceDao.findExistingIds(req.dataSourceIds());
            for (String dsId : req.dataSourceIds()) {
                if (!existingIds.contains(dsId)) {
                    throw new ValidationException("Validation failed",
                            Map.of("dataSourceIds", "Data source not found: " + dsId));
                }
            }
        }

        String id = segmentDao.nextId();
        String status = req.status() != null ? req.status() : "draft";

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Segment segment = segmentDao.insert(conn, id, req.name().trim(),
                        req.description(), status, 0, BigDecimal.ZERO, "API");
                segmentDao.insertTags(conn, id, req.tags());
                segmentDao.insertDataSources(conn, id, req.dataSourceIds());
                conn.commit();

                // Re-read to get tags and data sources populated
                return segmentDao.findById(id).orElse(segment);
            } catch (Exception e) {
                conn.rollback();
                log.error("Failed to create segment", e);
                throw new RuntimeException("Failed to create segment", e);
            }
        } catch (SQLException e) {
            log.error("Failed to create segment", e);
            throw new RuntimeException("Failed to create segment", e);
        }
    }
}
