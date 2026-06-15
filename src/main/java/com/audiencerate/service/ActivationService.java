package com.audiencerate.service;

import com.audiencerate.dao.ActivationDao;
import com.audiencerate.dao.DestinationDao;
import com.audiencerate.dao.SegmentDao;
import com.audiencerate.error.NotFoundException;
import com.audiencerate.model.Activation;
import com.audiencerate.model.Destination;
import com.audiencerate.model.response.PagedResponse;
import com.audiencerate.model.response.PaginationMeta;
import com.audiencerate.model.request.CreateActivationRequest;
import com.audiencerate.validation.ActivationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivationService {

    private static final Logger LOG = LoggerFactory.getLogger(ActivationService.class);
    private static final String ERR_CREATE_ACTIVATION = "Failed to create activation";
    private final ActivationDao activationDao;
    private final DestinationDao destinationDao;
    private final SegmentDao segmentDao;
    private final ActivationValidator validator;
    private final DataSource activationsDs;

    public ActivationService(ActivationDao activationDao, DestinationDao destinationDao,
                             SegmentDao segmentDao, ActivationValidator validator,
                             DataSource activationsDs) {
        this.activationDao = activationDao;
        this.destinationDao = destinationDao;
        this.segmentDao = segmentDao;
        this.validator = validator;
        this.activationsDs = activationsDs;
    }

    public PagedResponse<Activation> list(String segmentId, String destinationId, int page, int pageSize) {
        ActivationDao.ActivationListResult result = activationDao.list(segmentId, destinationId, page, pageSize);
        List<Activation> enriched = enrichWithDestinations(result.data());
        int totalPages = (int) Math.ceil((double) result.total() / pageSize);
        return new PagedResponse<>(
                enriched,
                new PaginationMeta(page, pageSize, result.total(), totalPages));
    }

    public PagedResponse<Activation> getActivationsForSegment(String segmentId, int page, int pageSize) {
        // Verify segment exists (segments DB)
        segmentDao.findById(segmentId)
                .orElseThrow(() -> new NotFoundException("Segment not found: %s".formatted(segmentId)));

        // Fetch activations (activations DB) with pagination
        ActivationDao.ActivationListResult result = activationDao.list(segmentId, null, page, pageSize);

        // Enrich with destination (activations DB)
        List<Activation> enriched = enrichWithDestinations(result.data());
        int totalPages = (int) Math.ceil((double) result.total() / pageSize);
        return new PagedResponse<>(
                enriched,
                new PaginationMeta(page, pageSize, result.total(), totalPages));
    }

    public Activation create(CreateActivationRequest req) {
        validator.validateCreate(req);

        // Create activation
        try (Connection conn = activationsDs.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Activation activation = activationDao.create(conn, req.segmentId(), req.destinationId());
                conn.commit();

                // Enrich with destination
                Destination destination = destinationDao.findById(activation.getDestinationId()).orElse(null);
                activation.setDestination(destination);
                return activation;
            } catch (Exception e) {
                conn.rollback();
                LOG.error(ERR_CREATE_ACTIVATION, e);
                throw new RuntimeException(ERR_CREATE_ACTIVATION, e);
            }
        } catch (SQLException e) {
            LOG.error(ERR_CREATE_ACTIVATION, e);
            throw new RuntimeException(ERR_CREATE_ACTIVATION, e);
        }
    }

    private List<Activation> enrichWithDestinations(List<Activation> activations) {
        Map<String, Destination> destMap = destinationDao.findAll().stream()
                .collect(Collectors.toMap(Destination::id, destination -> destination));

        for (Activation activation : activations) {
            activation.setDestination(destMap.get(activation.getDestinationId()));
        }
        return activations;
    }
}
