package com.audiencerate.service;

import com.audiencerate.dao.ActivationDao;
import com.audiencerate.dao.DestinationDao;
import com.audiencerate.dao.SegmentDao;
import com.audiencerate.error.NotFoundException;
import com.audiencerate.error.ValidationException;
import com.audiencerate.model.Activation;
import com.audiencerate.model.Destination;
import com.audiencerate.model.response.PagedResponse;
import com.audiencerate.model.response.PaginationMeta;
import com.audiencerate.model.request.CreateActivationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivationService {

    private static final Logger log = LoggerFactory.getLogger(ActivationService.class);
    private final ActivationDao activationDao;
    private final DestinationDao destinationDao;
    private final SegmentDao segmentDao;

    public ActivationService(ActivationDao activationDao, DestinationDao destinationDao, SegmentDao segmentDao) {
        this.activationDao = activationDao;
        this.destinationDao = destinationDao;
        this.segmentDao = segmentDao;
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
                .orElseThrow(() -> new NotFoundException("Segment not found: " + segmentId));

        // Fetch activations (activations DB) with pagination
        ActivationDao.ActivationListResult result = activationDao.list(segmentId, null, page, pageSize);

        // Enrich with destination (activations DB)
        List<Activation> enriched = enrichWithDestinations(result.data());
        int totalPages = (int) Math.ceil((double) result.total() / pageSize);
        return new PagedResponse<>(
                enriched,
                new PaginationMeta(page, pageSize, result.total(), totalPages));
    }

    public Activation create(CreateActivationRequest req, DataSource activationsDs) {
        Map<String, String> errors = new java.util.LinkedHashMap<>();
        if (req.segmentId() == null || req.segmentId().isBlank()) {
            errors.put("segmentId", "Segment ID is required");
        }
        if (req.destinationId() == null || req.destinationId().isBlank()) {
            errors.put("destinationId", "Destination ID is required");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }

        // Validate segment exists (segments DB)
        if (segmentDao.findById(req.segmentId()).isEmpty()) {
            throw new ValidationException("Validation failed",
                    Map.of("segmentId", "Segment not found: " + req.segmentId()));
        }

        // Validate destination exists (activations DB)
        List<Destination> destinations = destinationDao.findAll();
        boolean destExists = destinations.stream()
                .anyMatch(d -> d.id().equals(req.destinationId()));
        if (!destExists) {
            throw new ValidationException("Validation failed",
                    Map.of("destinationId", "Destination not found: " + req.destinationId()));
        }

        // Create activation
        try (Connection conn = activationsDs.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Activation activation = activationDao.create(conn, req.segmentId(), req.destinationId());
                conn.commit();

                // Enrich with destination
                Destination dest = destinations.stream()
                        .filter(d -> d.id().equals(activation.getDestinationId()))
                        .findFirst().orElse(null);
                activation.setDestination(dest);
                return activation;
            } catch (Exception e) {
                conn.rollback();
                log.error("Failed to create activation", e);
                throw new RuntimeException("Failed to create activation", e);
            }
        } catch (SQLException e) {
            log.error("Failed to create activation", e);
            throw new RuntimeException("Failed to create activation", e);
        }
    }

    private List<Activation> enrichWithDestinations(List<Activation> activations) {
        Map<String, Destination> destMap = destinationDao.findAll().stream()
                .collect(Collectors.toMap(Destination::id, d -> d));

        for (Activation activation : activations) {
            activation.setDestination(destMap.get(activation.getDestinationId()));
        }
        return activations;
    }
}
