package com.audiencerate.service;

import com.audiencerate.dao.SegmentDao;
import com.audiencerate.dao.SegmentDao.SegmentListResult;
import com.audiencerate.dao.SegmentTrendDao;
import com.audiencerate.error.NotFoundException;
import com.audiencerate.model.Segment;
import com.audiencerate.model.SegmentTrendPoint;
import com.audiencerate.model.request.CreateSegmentRequest;
import com.audiencerate.model.request.UpdateSegmentRequest;
import com.audiencerate.model.response.PagedResponse;
import com.audiencerate.model.response.PaginationMeta;
import com.audiencerate.validation.SegmentValidator;

import java.util.List;

public class SegmentService {

    private final SegmentDao segmentDao;
    private final SegmentTrendDao trendDao;
    private final SegmentValidator validator;

    public SegmentService(SegmentDao segmentDao, SegmentTrendDao trendDao, SegmentValidator validator) {
        this.segmentDao = segmentDao;
        this.trendDao = trendDao;
        this.validator = validator;
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
                .orElseThrow(() -> new NotFoundException("Segment not found: %s".formatted(id)));
    }

    public List<SegmentTrendPoint> getTrend(String id, int rangeDays) {
        // Verify segment exists
        if (segmentDao.findById(id).isEmpty()) {
            throw new NotFoundException("Segment not found: %s".formatted(id));
        }
        validator.validateTrendRange(rangeDays);
        return trendDao.findBySegmentId(id, rangeDays);
    }

    public Segment update(String id, UpdateSegmentRequest req) {
        validator.validateUpdate(req);
        return segmentDao.update(id, req.name() != null ? req.name().trim() : null,
                        req.description(), req.status(),
                        req.tags(), req.dataSourceIds())
                .orElseThrow(() -> new NotFoundException("Segment not found: %s".formatted(id)));
    }

    public void delete(String id) {
        if (!segmentDao.delete(id)) {
            throw new NotFoundException("Segment not found: %s".formatted(id));
        }
    }

    public Segment create(CreateSegmentRequest req) {
        validator.validateCreate(req);

        String status = req.status() != null ? req.status() : "draft";

        return segmentDao.create(req.name().trim(), req.description(), status,
                req.tags(), req.dataSourceIds());
    }
}
